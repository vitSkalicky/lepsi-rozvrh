package cz.vitskalicky.lepsirozvrh.bakaAPI.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.module.kotlin.readValue
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.R
import cz.vitskalicky.lepsirozvrh.SharedPrefs
import cz.vitskalicky.lepsirozvrh.activity.LoginActivity
import cz.vitskalicky.lepsirozvrh.activity.MainActivity
import cz.vitskalicky.lepsirozvrh.activity.WelcomeActivity
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.Login.LoginResult.*
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification
import cz.vitskalicky.lepsirozvrh.widget.WidgetProvider
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.IOException
import kotlin.reflect.KClass


class Login(val app: MainApplication) {

    private val sprefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)

    /**
     * Returns a new retrofit which does not inject login token.
     */
    fun getUnloggedRetrofit(baseUrl: String): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(MainApplication.objectMapper))
                .client(client)
                .build()

    }

    /**
     * Returns a valid access token or null (if network not available) or throw [LoginRequiredException] if not logged in.
     * @throws LoginRequiredException if not logged in
     */
    suspend fun getAccessToken(): String? {
        if (sprefs.getString(SharedPrefs.ACCEESS_TOKEN, "").isNullOrBlank() ||
                sprefs.getString(SharedPrefs.REFRESH_TOKEN, "").isNullOrBlank() ||
                sprefs.getString(SharedPrefs.ACCESS_EXPIRES, "").isNullOrBlank()){
            throw LoginRequiredException()
        }

        val expiresStr: String = sprefs.getString(SharedPrefs.ACCESS_EXPIRES, null)!!
        val expires: LocalDateTime = LocalDateTime.parse(expiresStr, ISODateTimeFormat.dateTimeParser())

        if (expires.isAfter(LocalDateTime.now())){
            return sprefs.getString(SharedPrefs.ACCEESS_TOKEN, null)
        }

        val refreshStatus: LoginResult = refreshToken()
        when (refreshStatus){
            SUCCESS -> {
                return sprefs.getString(SharedPrefs.ACCEESS_TOKEN, null)
            }
            WRONG_LOGIN -> {
                throw LoginRequiredException()
            }
            else -> return null

        }
    }

    suspend fun handleException(e: Exception, whichAPI: String): LoginResult{
        when (e) {
            is HttpException -> {
                //probably could not parse the response
                //parse error body
                var parseException: IOException? = null
                var rawBody: String? = null;
                val errorBody: Map<String, Any>? = e.response()?.errorBody()?.let {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    withContext(Dispatchers.IO) {
                        try {
                            val str = it.string()
                            rawBody = str
                            MainApplication.objectMapper.readValue(str)
                        } catch (e: IOException) {
                            parseException = e
                            null
                        }
                    }
                }
                if (e.code() == 400 && errorBody?.get("error") == "invalid_grant") {
                    //wrong password username or refresh token
                    return WRONG_LOGIN
                }
                //unexpected - report
                Sentry.capture(IOException("Unexpected $whichAPI API response. Raw response: \'${rawBody}\' Message of exception while parsing (which is also set as cause of this exception): \'${parseException?.message}\'", parseException))
                return UNEXPECTED_RESPONSE
            }
            is IOException ->
                return UNREACHABLE
            else -> {
                throw e
            }
        }
    }

    suspend fun refreshToken(): LoginResult {
        val refreshToken: String = sprefs.getString(SharedPrefs.REFRESH_TOKEN, null)?.takeUnless { it.isBlank() } ?: return WRONG_LOGIN

        val retrofit: Retrofit = app.noAuthRetrofit!!
        val webservice: LoginWebservice = retrofit.create(LoginWebservice::class.java)

        try {
            val response: LoginResponse = webservice.refreshLogin(refreshToken)

            sprefs.edit().apply {
                putString(SharedPrefs.REFRESH_TOKEN, response.refresh_token)
                putString(SharedPrefs.ACCEESS_TOKEN, response.access_token)
                putString(SharedPrefs.ACCESS_EXPIRES, LocalDateTime.now().plusSeconds(response.expires_in).toString(ISODateTimeFormat.dateTime()))
            }.apply()

            //check if user info should be refreshed
            val semesterEnd: DateTime? = sprefs.getString(SharedPrefs.SEMESTER_END, null)?.takeUnless { it.isBlank() }?.let {ISODateTimeFormat.dateTime().parseDateTime(it)}
            if (semesterEnd == null || semesterEnd.isBeforeNow){
                refreshUserInfo()
            }

            return SUCCESS
        }catch (e: HttpException){
            return handleException(e, "login")
        }catch (e: IOException){
            return handleException(e, "login")
        }
    }

    suspend fun firstLogin(url: String, username: String, password: String): LoginResult{
        val url: String = unifyUrl(url)
        val webservice = getUnloggedRetrofit(url).create(LoginWebservice::class.java)

        try {
            val response: LoginResponse = webservice.firstLogin(username, password)
            //handle success

            sprefs.edit().apply {
                putString(SharedPrefs.REFRESH_TOKEN, response.refresh_token)
                putString(SharedPrefs.ACCEESS_TOKEN, response.access_token)
                putString(SharedPrefs.ACCESS_EXPIRES, LocalDateTime.now().plusSeconds(response.expires_in).toString(ISODateTimeFormat.dateTime()))
                putString(SharedPrefs.URL, url)
            }.apply()

            refreshUserInfo()

            return SUCCESS
        }catch (e: HttpException){
            return handleException(e, "login")
        }catch (e: IOException){
            return handleException(e, "login")
        }
    }

    suspend fun refreshUserInfo(): LoginResult{

        val userWebservice: UserWebservice = app.retrofit?.create(UserWebservice::class.java)!!
        try {
            val user: UserResponse = userWebservice.getUser()

            sprefs.edit().apply {
                putString(SharedPrefs.NAME, user.fullName ?: "")
                putString(SharedPrefs.TYPE, user.userType ?: "")
                putString(SharedPrefs.TYPE_TEXT, user.userTypeText ?: "")
                val semesterEnd: DateTime? = user.settingModules?.common?.actualSemester?.to?.let {
                    try {
                        ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(it)
                    }catch (e: IllegalArgumentException){
                        e.printStackTrace()
                        null
                    }
                }
                putString(SharedPrefs.SEMESTER_END, if (semesterEnd == null) "" else ISODateTimeFormat.dateTime().print(semesterEnd))
            }.apply()
            return SUCCESS
        }catch (e: HttpException){
            return handleException(e, "user")
        }catch (e: IOException){
            return handleException(e, "user")
        }
    }

    /**
     * Logs out user (deletes credentials)
     */
    fun logout() {
        sprefs.edit().apply {
            remove(SharedPrefs.REFRESH_TOKEN)
            remove(SharedPrefs.ACCEESS_TOKEN)
            remove(SharedPrefs.ACCESS_EXPIRES)
            remove(SharedPrefs.NAME)
            remove(SharedPrefs.TYPE)
            remove(SharedPrefs.TYPE_TEXT)
            remove(SharedPrefs.SEMESTER_END)
        }.apply()
        GlobalScope.launch {
            app.rozvrhDb.clearAllTables()
        }
        app.rozvrhStatusStore.clear()
        app.clearObjects()
        PermanentNotification.update(null, 0, app)
        WidgetProvider.updateAll(null, app)
    }

    fun isLoggedIn(): Boolean {
        return ! sprefs.getString(SharedPrefs.REFRESH_TOKEN, "").isNullOrBlank()
    }

    /**
     * Whether to show teacher's or students rozvrh (each is fetched and displayed slightly differently)
     * @return `true` if the user logged in is a teacher or `false` if not (then it is a student or a parent)
     */
    fun isTeacher(): Boolean {
        val type = sprefs.getString(SharedPrefs.TYPE, "")
        return type == "teacher"
    }

    /**
     * Checks if user is logged in or has seen the welcome screen (where crash reports are
     * enabled/disabled), the starts the corresponding activity (if it isn't already started).
     * `finish()` **won't** be called on the current activity.
     *
     * @return An activity which is being started or `null` if no activity will be started.
     */
    fun checkLogin(currentActivity: Activity): KClass<out Activity>? {
        val ctx = currentActivity
        val seenWelcome = SharedPrefs.containsPreference(app, R.string.PREFS_SEND_CRASH_REPORTS)
        if (!seenWelcome && currentActivity !is WelcomeActivity) {
            val intent = Intent(ctx, WelcomeActivity::class.java)
            ctx.startActivity(intent)
            return WelcomeActivity::class
        }
        if (!isLoggedIn() && currentActivity !is LoginActivity) {
            val intent = Intent(ctx, LoginActivity::class.java)
            ctx.startActivity(intent)
            return LoginActivity::class
        }
        if (currentActivity !is MainActivity) {
            val intent = Intent(ctx, MainActivity::class.java)
            ctx.startActivity(intent)
            return MainActivity::class
        }
        return null
    }

    companion object{
        /**
         * Removes /next/login.aspx
         */
        private fun unifyUrl(url: String): String {
            var url = url
            if (url.endsWith(".aspx")) url = url.substring(0, url.length - 5)
            if (url.endsWith("login")) {
                url = url.substring(0, url.length - 5)
                if (url.endsWith("next/")) url = url.substring(0, url.length - 5)
            }
            if (!url.endsWith("/")) url += "/"
            if (!(url.startsWith("http://") || url.startsWith("https://"))) {
                url = "https://$url"
            }
            return url
        }
    }

    enum class LoginResult{
        SUCCESS,
        UNREACHABLE,
        WRONG_LOGIN,
        UNEXPECTED_RESPONSE
    }
}

public class LoginRequiredException(): RuntimeException("You need to log in first to perform this action")
package cz.vitskalicky.lepsirozvrh.bakaAPI.login

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.module.kotlin.readValue
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.SharedPrefs
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.Login.LoginResult.*
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification
import cz.vitskalicky.lepsirozvrh.stringLiveData
import cz.vitskalicky.lepsirozvrh.widget.WidgetProvider
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.IOException

class Login(val app: MainApplication) {

    private val sprefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)

    public val accessTokenLD: LiveData<String?> by lazy { sprefs.stringLiveData(SharedPrefs.ACCEESS_TOKEN, null) }

    /**
     * Returns a new retrofit which does not inject login token.
     */
    fun getUnloggedRetrofit(baseUrl: String): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(app.objectMapper))
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

    suspend fun handleException(e: Exception): LoginResult{
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
                            app.objectMapper.readValue(str)
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
                Sentry.capture(IOException("Unexpected login API response. Raw response: \'${rawBody}\' Message of exception while parsing (which is also set as cause of this exception): \'${parseException?.message}\'", parseException))
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

        val retrofit: Retrofit = app.retrofit ?: getUnloggedRetrofit(sprefs.getString(SharedPrefs.URL, null)?.takeUnless { it.isBlank() }
                ?: return WRONG_LOGIN)
        val webservice: LoginWebservice = retrofit.create(LoginWebservice::class.java)

        try {
            val response: LoginResponse = webservice.refreshLogin(refreshToken)

            sprefs.edit().apply {
                putString(SharedPrefs.REFRESH_TOKEN, response.refresh_token)
                putString(SharedPrefs.ACCEESS_TOKEN, response.access_token)
                putString(SharedPrefs.ACCESS_EXPIRES, LocalDateTime.now().plusSeconds(response.expires_in).toString(ISODateTimeFormat.dateTime()))
            }.apply()
            return SUCCESS
        }catch (e: HttpException){
            return handleException(e)
        }catch (e: IOException){
            return handleException(e)
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
            return SUCCESS
        }catch (e: HttpException){
            return handleException(e)
        }catch (e: IOException){
            return handleException(e)
        }
    }

    /**
     * Logs out user (deletes credentials)
     */
    fun logout() {
        sprefs.edit().apply {
            remove(SharedPrefs.USERNAME)
            remove(SharedPrefs.REFRESH_TOKEN)
            remove(SharedPrefs.ACCEESS_TOKEN)
            remove(SharedPrefs.ACCESS_EXPIRES)
            remove(SharedPrefs.URL)
            remove(SharedPrefs.NAME)
            remove(SharedPrefs.TYPE)
        }.apply()
        app.rozvrhDb.clearAllTables()
        PermanentNotification.update(null, 0, app)
        WidgetProvider.updateAll(null, app)
    }

    fun isLoggedIn(): Boolean {
        return ! sprefs.getString(SharedPrefs.REFRESH_TOKEN, "").isNullOrBlank()
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
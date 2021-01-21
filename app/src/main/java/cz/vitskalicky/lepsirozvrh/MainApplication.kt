package cz.vitskalicky.lepsirozvrh

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jaredrummler.cyanea.Cyanea
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.Login
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.LoginRequiredException
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.TokenAuthenticator
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhRepository
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhWebservice
import cz.vitskalicky.lepsirozvrh.database.RozvrhDatabase
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.notification.NotificationState
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification
import cz.vitskalicky.lepsirozvrh.theme.DefaultThemes
import cz.vitskalicky.lepsirozvrh.theme.SystemTheme
import cz.vitskalicky.lepsirozvrh.theme.Theme
import cz.vitskalicky.lepsirozvrh.widget.WidgetProvider
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import io.sentry.event.User
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.LocalDateTime
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.*

class MainApplication : MultiDexApplication() {

    companion object {
        private val TAG = MainApplication::class.java.simpleName
        //private var _jacksonObjectMapper: ObjectMapper? = null
        public val objectMapper: ObjectMapper by lazy {
            val objectMapper = ObjectMapper()
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            objectMapper.registerModule(JodaModule())
            objectMapper.registerModule(KotlinModule())
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }

    private val tohle = this
    public val mainScope = MainScope()
    lateinit var notificationState: NotificationState
        private set
    private var updateTime: LocalDateTime? = null
    private lateinit var currentWeekLivedata: LiveData<RozvrhRelated>
    private lateinit var currentWeekObserver: Observer<RozvrhRelated>

    /**
     * Warning: never keep an instance! Always get one using [MainApplication.retrofit] to make sure it uses the current URL even after logout.
     */
    var retrofit: Retrofit? = null
        get() {
            if (SharedPrefs.contains(this, SharedPrefs.URL)) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                val tokenAuthenticator = TokenAuthenticator(this)
                val client = OkHttpClient.Builder()
                        .addInterceptor(interceptor)
                        .addInterceptor(tokenAuthenticator)
                        .authenticator(tokenAuthenticator)
                        .build()
                field = try {
                    Retrofit.Builder()
                            .baseUrl(SharedPrefs.getString(this, SharedPrefs.URL))
                            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                            .client(client)
                            .build()
                } catch (e: IllegalArgumentException) {
                    return null
                }
                return field
            }
            return null
        }
    private set

    /**
     * note: this retrofit is bound to the url, but does not authenticate
     * Warning: never keep an instance! Always get one using [MainApplication.noAuthRetrofit] to make sure it uses the current URL even after logout.
     */
    var noAuthRetrofit: Retrofit? = null
        get() {
            if (field != null)
                return field

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
            field = Retrofit.Builder()
                        .baseUrl(SharedPrefs.getString(this, SharedPrefs.URL))
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .client(client)
                        .build()

            return field
        }
    private set

    val rozvrhDb: RozvrhDatabase by lazy {
        Room.databaseBuilder(
                applicationContext,
                RozvrhDatabase::class.java, "rozvrh-database"
        ).build()
    }

    /**
     * Warning: never keep an instance! Always get one using [MainApplication.retrofit] to make sure it uses the current URL even after logout.
     */
    var webservice: RozvrhWebservice? = null
        get() = field ?: retrofit?.create(RozvrhWebservice::class.java)
        private set

    val repository: RozvrhRepository by lazy {
        RozvrhRepository(this)
    }

    val login: Login by lazy {
        Login(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Cyanea theme engine
        Cyanea.init(this, resources)

        // Initialize the Sentry (crash report) client
        if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_SEND_CRASH_REPORTS)) {
            enableSentry()
        } else {
            diableSentry()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Register notification channel for the permanent notification
            val name: CharSequence = getString(R.string.notification_channel_name)
            val description = getString(R.string.notification_detials)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(PermanentNotification.PERMANENT_CHANNEL_ID, name, importance)
            channel.description = description
            channel.setSound(Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.silence), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            channel.setShowBadge(false)
            channel.vibrationPattern = null
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        notificationState = NotificationState(this)
        if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_NOTIFICATION, true)) {
            enableNotification()
        } else {
            disableNotification()
        }
        val rozvrhAPI = AppSingleton.getInstance(this).rozvrhAPI
        currentWeekObserver = Observer { rozvrh: RozvrhRelated ->
            /*if (rozvrhWrapper!!.oldRozvrh != null) {
                WidgetProvider.updateAll(rozvrhWrapper.oldRozvrh, this)
                if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_NOTIFICATION, true)) {
                    PermanentNotification.update(rozvrhWrapper.oldRozvrh, this)
                }
            }
            updateUpdateTime(rozvrhWrapper.oldRozvrh)*/
            WidgetProvider.updateAll(rozvrh,this)
            if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_NOTIFICATION, true)) {
                PermanentNotification.update(rozvrh, this)
            }
            updateUpdateTime(rozvrh)
        }

        currentWeekLivedata = repository.getCurrentWeekLD()
        currentWeekLivedata.observeForever(currentWeekObserver)
        if (!SharedPrefs.containsPreference(this, R.string.PREFS_THEME_cHBg)) {
            //theme not initialized yet (first start or after update from pre-themes version)
            SharedPrefs.setStringPreference(this, R.string.PREFS_APP_THEME, "0")
            SharedPrefs.setBooleanPreference(this, R.string.PREFS_FOLLOW_SYSTEM_THEME, true)
            SharedPrefs.setBooleanPreference(this, R.string.PREFS_IS_DARK_THEME_FOR_SYSTEM_APPLIED, false)
            Theme.of(this).themeData = DefaultThemes.getLightTheme()
            Theme.of(this).checkSystemTheme()
        }
        if (SharedPrefs.getInt(this, SharedPrefs.LAST_VERSION_SEEN) < BuildConfig.VERSION_CODE) {
            //a new version is here
            // LAST_VERSION_SEEN is set by MainActivity

            //reapply default theme in case it changed
            var themeNumber = 4
            try {
                themeNumber = SharedPrefs.getStringPreference(this, R.string.PREFS_APP_THEME).toInt()
            } catch (ignored: NumberFormatException) {
            } catch (ignored: NullPointerException) {
            }
            val theme = Theme.of(this)
            when (themeNumber) {
                0 -> {
                    val systemIsDark = SystemTheme.isDarkTheme(this)
                    if (systemIsDark) {
                        theme.themeData = DefaultThemes.getDarkTheme()
                    } else {
                        theme.themeData = DefaultThemes.getLightTheme()
                    }
                    SharedPrefs.setBooleanPreference(this, R.string.PREFS_IS_DARK_THEME_FOR_SYSTEM_APPLIED, systemIsDark)
                }
                1 -> theme.themeData = DefaultThemes.getLightTheme()
                2 -> theme.themeData = DefaultThemes.getDarkTheme()
                3 -> theme.themeData = DefaultThemes.getBlackTheme()
            }
        }
    }

    fun scheduleUpdate(triggerTime: LocalDateTime?) {
        var triggerTime: LocalDateTime? = triggerTime

        if (notificationState.offsetResetTime != null && triggerTime?.isAfter(notificationState.offsetResetTime) ?: true) {
            triggerTime = notificationState.offsetResetTime
        }
        if (triggerTime == updateTime){
            return
        }
        val intent = Intent(this, UpdateBroadcastReciever::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, UpdateBroadcastReciever.REQUEST_CODE, intent, 0)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        var type: Int = AlarmManager.RTC_WAKEUP;
        if (triggerTime == null){
            type = AlarmManager.RTC
            triggerTime = LocalDateTime.now().plusHours(1)
        }
        alarmManager.setRepeating(type, triggerTime!!.toDate().time, (60 * 60000).toLong(), pendingIntent)
        Log.d(TAG, "Scheduled an update on " + triggerTime.toString("MM-dd HH:mm:ss"))
        updateTime = triggerTime
    }

    /**
     * Updates the widget and notification update time using the data from the given Rozvrh. !!! Use [.updateUpdateTime], because that one accounts for week shift during weekend !!!
     *
     * @return true if updated, false if the update time could not be determined from the given rozvrh.
     */
    private fun updateUpdateTime(rozvrh: RozvrhRelated): Boolean {
        val time = rozvrh.getUpdateDisplayedDataTime() ?: return false

        scheduleUpdate(time)

        return true
    }

    suspend fun updateUpdateTime() {
        val time: LocalDateTime? = repository.getUpdateDisplayedDataTime()
        scheduleUpdate(time)
    }

    fun enableNotification() {
        SharedPrefs.setBoolean(this, getString(R.string.PREFS_NOTIFICATION), true)
        PermanentNotification.update(currentWeekLivedata.value, this)
    }

    fun disableNotification() {
        SharedPrefs.setBoolean(this, getString(R.string.PREFS_NOTIFICATION), false)
        PermanentNotification.update(null, 0, this)
    }

    /**
     * Starts up sentry crash reporting, but only if it is an official build and crash reporting is
     * allowed (see build.gradle).
     */
    fun enableSentry() {
        /*
         * Only enable sentry on the official release build
         */
        if (BuildConfig.ALLOW_SENTRY) {
            Sentry.init("https://d13d732d380444f5bed7487cfea65814@sentry.io/1820627", AndroidSentryClientFactory(this))
            Sentry.getContext().addExtra("commit hash", BuildConfig.GitHash)
            if (!SharedPrefs.contains(this, SharedPrefs.SENTRY_ID) || SharedPrefs.getString(this, SharedPrefs.SENTRY_ID).isEmpty()) {
                SharedPrefs.setString(this, SharedPrefs.SENTRY_ID, "android:" + java.lang.Long.toHexString(Random().nextLong()))
            }
            Sentry.getContext().user = User(SharedPrefs.getString(this, SharedPrefs.SENTRY_ID), null, null, null)
        } else {
            diableSentry()
            SharedPrefs.setBooleanPreference(this, R.string.PREFS_SEND_CRASH_REPORTS, false)
        }
    }

    fun diableSentry() {
        Sentry.close()
    }

    /**
     * Call this after logout to clear all objects that might have saved url and credentials
     */
    fun clearObjects(){
        retrofit = null;
        webservice = null;
        noAuthRetrofit = null;
    }

    override fun onTerminate() {
        //prevent leaks
        currentWeekLivedata.removeObserver(currentWeekObserver)
        mainScope.cancel()
        super.onTerminate()
    }
}
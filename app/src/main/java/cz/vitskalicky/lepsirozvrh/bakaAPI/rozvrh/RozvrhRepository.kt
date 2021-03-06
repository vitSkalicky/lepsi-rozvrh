package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.LoginRequiredException
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhWebservice.Companion.getSchedule
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3.Rozvrh3
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3.RozvrhConverter
import cz.vitskalicky.lepsirozvrh.database.RozvrhDatabase
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatus
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatusStore
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import io.sentry.Sentry
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import retrofit2.HttpException
import java.io.IOException

class RozvrhRepository(context: Context, scope: CoroutineScope? = null) {
    private val application: MainApplication = context.applicationContext as MainApplication
    private val db: RozvrhDatabase = application.rozvrhDb
    private val statusStr: RozvrhStatusStore = application.rozvrhStatusStore
    private val scope: CoroutineScope = scope ?: application.mainScope

    private val currentWeekLD: MutableLiveData<RozvrhRelated> = MutableLiveData()

    fun getCurrentWeekLD(): LiveData<RozvrhRelated>{
        if (currentWeekLD.value == null){
            scope.launch {
                currentWeekLD.value = getRozvrh(Utils.getCurrentMonday(), foreground = false)
            }
        }
        return currentWeekLD
    }

    fun getRozvrhLive(rozvrhId: LocalDate, foreground: Boolean): LiveData<RozvrhRelated> {
        //fail-safe
        val rozvrhMonday: LocalDate = Utils.getWeekMonday(rozvrhId)

        refresh(rozvrhMonday, foreground, false)
        return db.rozvrhDao().loadRozvrhRelatedLive(rozvrhMonday)
    }

    fun refresh(rozvrhMonday: LocalDate, foreground: Boolean, force: Boolean){
        scope.launch(){
            if (force || refreshNeeded(rozvrhMonday, foreground)){
                try{
                    fetchAndCache(rozvrhMonday)
                }catch (e: Exception){
                    e.printStackTrace()
                    withContext(Dispatchers.Main){
                        reportError(e, rozvrhMonday)
                    }
                }
            }
        }
    }

    suspend fun getRozvrh(rozvrhId: LocalDate, foreground: Boolean): RozvrhRelated?{
        val monday: LocalDate = Utils.getWeekMonday(rozvrhId)
        try {
            if (refreshNeeded(monday, foreground)){
                return fetchAndCache(monday)
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                reportError(e, rozvrhId)
            }
        }
        return db.rozvrhDao().loadRozvrhRelated(monday)
    }

    fun getRozvrhStatusLiveData(rozvrhId: LocalDate): LiveData<RozvrhStatus>{
        return statusStr.getLiveData(rozvrhId)
    }

    fun getOfflineStatusLiveData(): LiveData<Boolean>{
        return statusStr.isOffline
    }

    /**
     * Returns the time when data on widget and in notification should be updated. `null` means, that it could not be determined and should be checked again later.
     */
    suspend fun getUpdateDisplayedDataTime():LocalDateTime?{

        val current: RozvrhRelated? = getRozvrh(Utils.getCurrentMonday(), false)
        var time: LocalDateTime? = null
        if (current == null){
            return null
        }else{
            time = current.getUpdateDisplayedDataTime()
        }

        if (time == null){
            val next = getRozvrh(Utils.getCurrentMonday().plusWeeks(1), false)
            if (next == null){
                return null
            }else{
                time = next.getUpdateDisplayedDataTime() ?: Utils.getCurrentMonday().plusDays(13).toLocalDateTime(LocalTime.MIDNIGHT)
            }
        }
        return time
    }

    private suspend fun refreshNeeded(rozvrhId: LocalDate, foreground: Boolean = false): Boolean{
        if (statusStr[rozvrhId].status == RozvrhStatus.Status.ERROR){
            return true
        }
        if (statusStr[rozvrhId].status == RozvrhStatus.Status.LOADING){
            return false
        }
        val expireTime = if (foreground){
            //if refreshing for foreground (e.g. MainActivity), we want a very fresh schedule to have more consistent data
            DateTime.now().minusMinutes(10)
        }else{
            // if it is only for widget or notification, we don't want to drain battery
            DateTime.now().minusHours(3)
        }
        return db.rozvrhDao().isExpired(rozvrhId,expireTime) != false
    }

    /**
     * Fetches from network and saves to database.
     * @throws IOException on network error
     * @throws RozvrhConverter.RozvrhConversionException on rozvrh conversion failure
     * @throws HttpException (probably) on [Rozvrh3] parsing failure
     * @throws LoginRequiredException if authentication fails
     * @throws Exception on other error such as parse error
     */
    @Throws(Exception::class)
    private suspend fun fetchAndCache(rozvrhId: LocalDate): RozvrhRelated {
        withContext(Dispatchers.Main) {
            statusStr[rozvrhId] = RozvrhStatus.loading()
        }
        val rozvrh3: Rozvrh3 = try {
            application.webservice?.getSchedule(rozvrhId) ?: throw IOException("Webservice not ready")
        }catch (e: HttpException){
            if (e.code() == 401) //unauthorized
                throw LoginRequiredException()
            else
                throw e
        }

        val rozvrh = withContext(Dispatchers.IO){ RozvrhConverter.convert(rozvrh3, rozvrhId) }
        db.insertRozvrhRelated(rozvrh)
        if (rozvrh.rozvrh.id == Utils.getCurrentMonday()){
            withContext(Dispatchers.Main){
                currentWeekLD.value = rozvrh;
            }
        }
        withContext(Dispatchers.Main) {
            statusStr.isOffline.value = false
            statusStr[rozvrhId] = RozvrhStatus.success()
        }
        return rozvrh
    }

    /**
     * must run on UI thread
     */
    private fun reportError(e: Exception, rozvrhId: LocalDate) {
        statusStr.isOffline.value = true
        statusStr[rozvrhId] = when (e) {
            is IOException -> {
                //network error
                RozvrhStatus.unreachable()
            }
            is RozvrhConverter.RozvrhConversionException -> {
                //conversion failed
                Sentry.capture(e)
                RozvrhStatus.unexpectedResponse()
            }
            is LoginRequiredException -> {
                application.login.logout()
                RozvrhStatus.loginFailed()
            }
            is HttpException -> {
                //todo unexpected resoponse, probably
                RozvrhStatus.unexpectedResponse()
            }
            else -> {
                statusStr[rozvrhId] = RozvrhStatus.unexpectedResponse()
                Sentry.capture(e)
                throw e
                //other reasons
                //todo report parse error
            }
        }
    }

}

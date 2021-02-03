package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.R
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.LoginRequiredException
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhWebservice.Companion.getSchedule
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3.Rozvrh3
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3.RozvrhConverter
import cz.vitskalicky.lepsirozvrh.database.RozvrhDatabase
import cz.vitskalicky.lepsirozvrh.model.Resource
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatus
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import kotlinx.coroutines.*
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import retrofit2.HttpException
import java.io.IOException
import kotlin.Exception
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatus.Status.*
import cz.vitskalicky.lepsirozvrh.model.Resource.Status.*
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatusStore
import org.joda.time.DateTime

class RozvrhRepository(context: Context, scope: CoroutineScope? = null) {
    private val application: MainApplication = context.applicationContext as MainApplication
    private val db: RozvrhDatabase = application.rozvrhDb
    private val statusStr: RozvrhStatusStore = application.rozvrhStatusStore
    private val scope: CoroutineScope = scope ?: application.mainScope

    private val currentWeekLD: MutableLiveData<RozvrhRelated> = MutableLiveData()

    fun getCurrentWeekLD(): LiveData<RozvrhRelated>{
        if (currentWeekLD.value == null){
            refresh(Utils.getCurrentMonday())
        }
        return currentWeekLD
    }

    fun getRozvrhLive(rozvrhId: LocalDate): LiveData<RozvrhRelated> {
        //fail-safe
        val rozvrhMonday: LocalDate = Utils.getWeekMonday(rozvrhId)

        refresh(rozvrhMonday)
        return db.rozvrhDao().loadRozvrhRelatedLive(rozvrhMonday)
    }

    fun refresh(rozvrhMonday: LocalDate){
        scope.launch(){
            if (refreshNeeded(rozvrhMonday)){
                try{
                    fetchAndCache(rozvrhMonday)
                }catch (e: Exception){
                    e.printStackTrace()
                    reportError(e, rozvrhMonday)
                }
            }
        }
    }

    suspend fun getRozvrh(rozvrhId: LocalDate): RozvrhRelated?{
        val monday: LocalDate = Utils.getWeekMonday(rozvrhId)
        try {
            if (refreshNeeded(monday)){
                return fetchAndCache(monday)
            }
        }catch (e: Exception){
            reportError(e, rozvrhId)
        }
        return db.rozvrhDao().loadRozvrhRelated(monday)
    }

    fun getRozvrhStatusLiveData(rozvrhId: LocalDate): LiveData<RozvrhStatus>{
        return statusStr.getLiveData(rozvrhId)
    }

    /**
     * Returns the time when data on widget and in notification should be updated. `null` means, that it could not be determined and should be checked again later.
     */
    suspend fun getUpdateDisplayedDataTime():LocalDateTime?{

        val current: RozvrhRelated? = getRozvrh(Utils.getCurrentMonday())
        var time: LocalDateTime? = null
        if (current == null){
            return null
        }else{
            time = current.getUpdateDisplayedDataTime()
        }

        if (time == null){
            val next = getRozvrh(Utils.getCurrentMonday().plusWeeks(1))
            if (next == null){
                return null
            }else{
                time = next.getUpdateDisplayedDataTime() ?: Utils.getCurrentMonday().plusDays(13).toLocalDateTime(LocalTime.MIDNIGHT)
            }
        }
        return time
    }

    private suspend fun refreshNeeded(rozvrhId: LocalDate, foreground: Boolean = false): Boolean{
        val expireTime = if (foreground){
            //if refreshing for foreground (e.g. MainActivity), we want a very fresh schedule to have more consistent data
            DateTime.now().minusMinutes(10)
        }else{
            // if it is only for widget or notification, we don't want to drain battery
            DateTime.now().minusHours(3)
        }
        return db.rozvrhDao().loadRozvrh(rozvrhId)?.lastUpdate?.isBefore(expireTime) != false
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
        application.rozvrhStatusStore[rozvrhId] = RozvrhStatus.loading()
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
        application.rozvrhStatusStore[rozvrhId] = RozvrhStatus.success()
        return rozvrh
    }

    private fun reportError(e: Exception, rozvrhId: LocalDate) {
        statusStr[rozvrhId] = when (e) {
            is IOException -> {
                //network error
                RozvrhStatus.unreachable()
            }
            is RozvrhConverter.RozvrhConversionException -> {
                //conversion failed
                //todo report
                RozvrhStatus.unexpectedResponse()
            }
            is LoginRequiredException ->{
                //todo solve login problem
                RozvrhStatus.loginFailed()
            }
            is HttpException -> {
                //todo unexpected resoponse, probably
                RozvrhStatus.unexpectedResponse()
            }
            else -> {
                statusStr[rozvrhId] = RozvrhStatus.unexpectedResponse()
                throw e
                //other reasons
                //todo report parse error
            }
        }
    }

}

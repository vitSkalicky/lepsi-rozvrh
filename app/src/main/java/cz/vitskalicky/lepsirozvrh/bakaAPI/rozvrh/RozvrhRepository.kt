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
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import kotlinx.coroutines.*
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import retrofit2.HttpException
import java.io.IOException
import kotlin.Exception

class RozvrhRepository(context: Context, scope: CoroutineScope? = null) {
    private val application: MainApplication = context.applicationContext as MainApplication
    private val db: RozvrhDatabase = application.rozvrhDb
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
                    reportError(e)
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
            reportError(e)
        }
        return db.rozvrhDao().loadRozvrhRelated(monday)
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

    private suspend fun refreshNeeded(rozvrhId: LocalDate): Boolean{
        //TODO check age
        return true
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
            currentWeekLD.value = rozvrh;
        }
        return rozvrh
    }

    private fun reportError(e: Exception) {
        when (e) {
            is IOException -> {
                //network error
            }
            is RozvrhConverter.RozvrhConversionException -> {
                //conversion failed
                //todo report
            }
            is LoginRequiredException ->{
                //todo solve login problem
            }
            is HttpException -> {
                //todo unexpected resoponse, probably
            }
            else -> {
                throw e
                //other reasons
                //todo report parse error
            }
        }
    }

}

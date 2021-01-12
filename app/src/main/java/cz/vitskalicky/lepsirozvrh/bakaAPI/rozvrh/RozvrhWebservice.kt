package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh

import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3.Rozvrh3
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import org.joda.time.LocalDate
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RozvrhWebservice {

    companion object{
        var datePatter = "YYYY-MM-dd"

    }

    @GET("api/3/timetable/actual?")
    suspend fun getActualSchedule(@Query("date") date: String?): Rozvrh3

    suspend fun getActualSchedule(date: LocalDate): Rozvrh3{
        return getActualSchedule(date.toString(datePatter))
    }

    /**
     * fetches the schedule for given monday or permanent if [monday] is [Rozvrh.PERM].
     */
    suspend fun getSchedule(monday: LocalDate) = if (monday == Rozvrh.PERM) getPermanentSchedule() else getActualSchedule(Utils.getWeekMonday(monday))

    @GET("api/3/timetable/permanent")
    suspend fun getPermanentSchedule(): Rozvrh3
}
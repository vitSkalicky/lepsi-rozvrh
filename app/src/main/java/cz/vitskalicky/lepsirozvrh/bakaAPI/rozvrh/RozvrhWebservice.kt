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

        /**
         * fetches the schedule for given monday or permanent if [monday] is [Rozvrh.PERM].
         */
        suspend fun RozvrhWebservice.getSchedule(monday: LocalDate): Rozvrh3{
            if (monday == Rozvrh.PERM)
                return getPermanentSchedule()
            else
                return getActualSchedule(Utils.getWeekMonday(monday))
        }

        suspend fun RozvrhWebservice.getActualSchedule(date: LocalDate): Rozvrh3{
            return getActualSchedule(date.toString(datePatter))
        }
    }

    @GET("api/3/timetable/actual?")
    suspend fun getActualSchedule(@Query("date") date: String?): Rozvrh3

    @GET("api/3/timetable/permanent")
    suspend fun getPermanentSchedule(): Rozvrh3
}
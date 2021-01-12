package cz.vitskalicky.lepsirozvrh.model.rozvrh

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

@Entity
data class Rozvrh(
        /**
         * Monday of the week. [Rozvrh.PERM] for permanent schedule.
         */
        @PrimaryKey
        val id: LocalDate,
        val lastUpdate: DateTime,
        val permanent: Boolean,
        @Embedded(prefix = "cycle_")
        val cycle: RozvrhCycle?
){
        companion object{
                val PERM: LocalDate = LocalDate.parse("0000-01-01").plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY)
        }
}
package cz.vitskalicky.lepsirozvrh.model.rozvrh

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.LocalDate

@Entity
data class RozvrhDay(
        @PrimaryKey
        val date: LocalDate,
        val rozvrh: LocalDate,
        val dayOfWeek: Int,
        /**
         * For completely free days such as holiday
         */
        val event: String?
)
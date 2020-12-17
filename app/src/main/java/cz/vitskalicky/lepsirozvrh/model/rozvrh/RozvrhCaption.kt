package cz.vitskalicky.lepsirozvrh.model.rozvrh

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.LocalDate
import org.joda.time.LocalTime

@Entity
data class RozvrhCaption(
        val rozvrh: LocalDate,
        //anything
        @PrimaryKey
        val id: String,
        val name: String,
        val startTime: LocalTime,
        val endTime: LocalTime,
        //index of caption in the day
        val index: Int
)
package cz.vitskalicky.lepsirozvrh.model.rozvrh

import org.joda.time.LocalDate
import org.joda.time.LocalTime

data class RozvrhCaption(
        val rozvrh: LocalDate,
        //anything
        val id: String,
        val name: String,
        val startTime: LocalTime,
        val endTime: LocalTime,
        //index of caption in the day
        val index: Int
)
package cz.vitskalicky.lepsirozvrh.model.rozvrh

import org.joda.time.LocalDate

data class RozvrhBlock(
        val day: LocalDate,
        val caption: String,
        val lessons: List<RozvrhLesson>

)
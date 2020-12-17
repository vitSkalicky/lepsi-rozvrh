package cz.vitskalicky.lepsirozvrh.model.rozvrh

import org.joda.time.LocalDate

data class RozvrhDay(
        val date: LocalDate,
        val rozvrh: LocalDate,
        val dayOfWeek: Int,
        /**
         * For completely free days such as holiday
         */
        val event: String?
)
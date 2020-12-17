package cz.vitskalicky.lepsirozvrh.model.rozvrh

import org.joda.time.DateTime
import org.joda.time.LocalDate

data class Rozvrh(
        val id: LocalDate,
        val lastUpdate: DateTime,
        val permanent: Boolean,
        val cycle: RozvrhCycle
)
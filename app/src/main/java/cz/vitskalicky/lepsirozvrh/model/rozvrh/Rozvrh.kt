package cz.vitskalicky.lepsirozvrh.model.rozvrh

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import org.joda.time.LocalDate

@Entity
data class Rozvrh(
        @PrimaryKey
        val id: LocalDate,
        val lastUpdate: DateTime,
        val permanent: Boolean,
        @Embedded(prefix = "cycle_")
        val cycle: RozvrhCycle
)
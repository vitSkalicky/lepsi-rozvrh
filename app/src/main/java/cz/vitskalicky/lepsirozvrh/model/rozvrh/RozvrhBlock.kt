package cz.vitskalicky.lepsirozvrh.model.rozvrh

import androidx.room.Entity
import org.joda.time.LocalDate

@Entity(primaryKeys = ["day","caption"])
data class RozvrhBlock(
        val day: LocalDate,
        val caption: String,
        val lessons: List<RozvrhLesson>
)
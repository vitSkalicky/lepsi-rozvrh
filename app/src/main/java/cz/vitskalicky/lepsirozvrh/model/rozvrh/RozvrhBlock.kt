package cz.vitskalicky.lepsirozvrh.model.rozvrh

import androidx.room.Entity
import androidx.room.ForeignKey
import org.joda.time.LocalDate

@Entity(
        primaryKeys = ["day","caption"],
        foreignKeys = [
            ForeignKey(
                entity = RozvrhDay::class,
                parentColumns = arrayOf("date"),
                childColumns = arrayOf("day"),
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE,
                deferred = true
            ),
            ForeignKey(
                entity = RozvrhCaption::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("caption"),
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE,
                deferred = true
            )
        ]
)
data class RozvrhBlock(
        val day: LocalDate,
        val caption: String,
        val lessons: List<RozvrhLesson>
)
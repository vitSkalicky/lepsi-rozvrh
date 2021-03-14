package cz.vitskalicky.lepsirozvrh.model.rozvrh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import org.joda.time.LocalDate

@Entity(
        primaryKeys = ["id"],
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
        ],
        indices = [Index("day"), Index("caption"), Index("id")]

)
data class RozvrhBlock(
        val day: LocalDate,
        val caption: String,
        val id: String = "$day-$caption"
)
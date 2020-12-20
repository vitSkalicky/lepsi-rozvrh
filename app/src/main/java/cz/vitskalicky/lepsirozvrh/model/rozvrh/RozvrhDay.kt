package cz.vitskalicky.lepsirozvrh.model.rozvrh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.LocalDate

@Entity(
    foreignKeys = [ForeignKey(
            entity = Rozvrh::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("rozvrh"),
            onDelete = CASCADE,
            onUpdate = CASCADE,
            deferred = true
    )],
    indices = [Index("rozvrh")]
)
data class RozvrhDay(
    /**
     * if permanent, then it is a date from the week of [Rozvrh.PERM].
     */
    @PrimaryKey
    val date: LocalDate,
    val rozvrh: LocalDate,
    //val dayOfWeek: Int,
    /**
     * For completely free days such as holiday. If not `null`, then the whole day is free.
     */
    val event: String?
)
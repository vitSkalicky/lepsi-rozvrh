package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay

data class DayRelated (
        @Embedded val day: RozvrhDay,

        @Relation(
                entity = BlockRelated::class,
                parentColumn = "date",
                entityColumn = "day"
        )
        val blocks: List<BlockRelated>
)
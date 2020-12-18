package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay

data class DayRelated (
        @Embedded val day: RozvrhDay,

        @Relation(
                parentColumn = "date",
                entityColumn = "day",
                entity = RozvrhBlock::class
        )
        val blocks: List<BlockRelated>
)
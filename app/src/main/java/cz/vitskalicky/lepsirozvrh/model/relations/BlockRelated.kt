package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay

data class BlockRelated(
    @Embedded val block: RozvrhBlock,

    @Relation(
            entity = RozvrhCaption::class,
            parentColumn = "id",
            entityColumn = "caption"
    )
    val caption: RozvrhCaption

    /*@Relation(
            entity = RozvrhDay::class,
            parentColumn = "date",
            entityColumn = "day"
    )
    val day: RozvrhDay*/
)
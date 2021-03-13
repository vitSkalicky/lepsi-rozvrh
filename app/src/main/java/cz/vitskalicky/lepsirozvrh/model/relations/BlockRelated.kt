package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson

data class BlockRelated(
    @Embedded val block: RozvrhBlock,

    @Relation(
            parentColumn = "caption",
            entityColumn = "id",
            entity = RozvrhCaption::class
    )
    val caption: RozvrhCaption,

    @Relation(
            parentColumn = "id",
            entityColumn = "blockId",
            entity = RozvrhLesson::class
    )
    val lessons: List<RozvrhLesson>
    /*@Relation(
            entity = RozvrhDay::class,
            parentColumn = "date",
            entityColumn = "day"
    )
    val day: RozvrhDay*/
)
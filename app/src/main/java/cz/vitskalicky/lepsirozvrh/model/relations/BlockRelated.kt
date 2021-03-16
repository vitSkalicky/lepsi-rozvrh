package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import com.fasterxml.jackson.annotation.JsonGetter
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
    val lessons: Set<RozvrhLesson>
    /*@Relation(
            entity = RozvrhDay::class,
            parentColumn = "date",
            entityColumn = "day"
    )
    val day: RozvrhDay*/
){
    //would be better if sorted by the database, but this is simpler.
    @Ignore
    private lateinit var lessonsSortedVar: List<RozvrhLesson>
    /**
     * lessons sorted by index in block
     */
    @JsonGetter(value = "lessons")
    fun lessonsSorted(): List<RozvrhLesson> {
        if (!::lessonsSortedVar.isInitialized){
            lessonsSortedVar = lessons.sortedBy { it.indexInBlock }
        }
        return lessonsSortedVar
    }
}
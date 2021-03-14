package cz.vitskalicky.lepsirozvrh.database

import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.*
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.*

@Database(entities = [Rozvrh::class, RozvrhCaption::class, RozvrhDay::class, RozvrhBlock::class, RozvrhLesson::class], version = 1)
@TypeConverters(*[LocalDateConverters::class, LocalTimeConverters::class, DateTimeConverters::class, CycleConverters::class, GroupConverters::class, StringListConverters::class])
abstract class RozvrhDatabase : RoomDatabase() {
    abstract fun rozvrhDao(): RozvrhDao
    abstract fun captionDao(): CaptionDao
    abstract fun dayDao(): DayDao
    abstract fun blockDao(): BlockDao
    abstract fun lessonDao(): LessonDao


    suspend fun insertRozvrhRelated(vararg rozvrhs: RozvrhRelated){
        val captions = ArrayList<RozvrhCaption>()
        val days = ArrayList<RozvrhDay>()
        val blocks = ArrayList<RozvrhBlock>()
        val lessons = ArrayList<RozvrhLesson>()

        rozvrhs.forEach {
            captions.addAll(it.captions);
            days.addAll(it.days.map { it.day })
            it.days.forEach{
                blocks.addAll(it.blocks.map { it.block })
                it.blocks.forEach{
                    lessons.addAll(it.lessons)
                }
            }
        }

        withTransaction {
            rozvrhDao().deleteRozvrh(*rozvrhs.map{ it.rozvrh }.toTypedArray())
            rozvrhDao().insertRozvrh(*rozvrhs.map{ it.rozvrh }.toTypedArray())

            captionDao().insertCaption(*captions.toTypedArray())
            dayDao().insertRozvrhDay(*days.toTypedArray())
            blockDao().insertRozvrhBlock(*blocks.toTypedArray())
            lessonDao().insertRozvrhLesson(*lessons.toTypedArray())
        }
    }
}

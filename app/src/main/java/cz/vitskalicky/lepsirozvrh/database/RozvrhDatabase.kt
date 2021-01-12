package cz.vitskalicky.lepsirozvrh.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.vitskalicky.lepsirozvrh.model.DateTimeConverters
import cz.vitskalicky.lepsirozvrh.model.LessonConverters
import cz.vitskalicky.lepsirozvrh.model.LocalDateConverters
import cz.vitskalicky.lepsirozvrh.model.LocalTimeConverters
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay

@Database(entities = [Rozvrh::class, RozvrhBlock::class, RozvrhCaption::class, RozvrhDay::class], version = 1)
@TypeConverters(*[LocalDateConverters::class, LocalTimeConverters::class, DateTimeConverters::class,LessonConverters::class])
abstract class RozvrhDatabase : RoomDatabase() {
    @TypeConverters(*[LocalDateConverters::class, LocalTimeConverters::class, DateTimeConverters::class, LessonConverters::class])
    abstract fun rozvrhDao(): RozvrhDao
    abstract fun blockDao(): BlockDao
    abstract fun dayDao(): DayDao
    abstract fun captionDao(): CaptionDao


    suspend fun insertRozvrhRelated(vararg rozvrhs: RozvrhRelated){
        val captions = ArrayList<RozvrhCaption>()
        val days = ArrayList<RozvrhDay>()
        val blocks = ArrayList<RozvrhBlock>()

        rozvrhs.forEach {
            captions.addAll(it.captions);
            days.addAll(it.days.map { it.day })
            it.days.forEach{
                blocks.addAll(it.blocks.map { it.block })
            }
        }

        rozvrhDao().deleteRozvrh(*rozvrhs.map{ it.rozvrh }.toTypedArray())
        rozvrhDao().insertRozvrh(*rozvrhs.map{ it.rozvrh }.toTypedArray())

        captionDao().insertCaption(*captions.toTypedArray())
        dayDao().insertRozvrhDay(*days.toTypedArray())
        blockDao().insertRozvrhBlock(*blocks.toTypedArray())
    }
}

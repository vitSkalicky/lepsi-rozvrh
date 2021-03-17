package cz.vitskalicky.lepsirozvrh.database

import androidx.lifecycle.LiveData
import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson
import org.joda.time.LocalDate

@Dao
abstract class LessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRozvrhLesson(vararg blocks: RozvrhLesson)

    @Delete
    abstract suspend fun deleteRozvrhLesson(vararg blocks: RozvrhLesson)

    @Update
    abstract suspend fun updateRozvrhLesson(vararg blocks: RozvrhLesson)
}
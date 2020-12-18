package cz.vitskalicky.lepsirozvrh.database

import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.relations.DayRelated
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@Dao
abstract class DayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRozvrhDay(vararg days: RozvrhDay)

    @Delete
    abstract suspend fun deleteRozvrhDay(vararg days: RozvrhDay)

    @Update
    abstract suspend fun updateRozvrhDay(vararg days: RozvrhDay)

    @Query("SELECT * FROM rozvrhday WHERE date = :date")
    abstract fun loadRozvrhDay(date: LocalDate): Flow<RozvrhDay>

    @Transaction
    @Query("SELECT * FROM rozvrhday WHERE date = :date")
    abstract fun loadDayRelated(date: LocalDate): Flow<DayRelated>


}
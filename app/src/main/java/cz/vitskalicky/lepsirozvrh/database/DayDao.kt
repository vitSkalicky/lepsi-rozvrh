package cz.vitskalicky.lepsirozvrh.database

import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.relations.DayRelated
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
abstract class DayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRozvrhDay(vararg days: RozvrhDay)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDayRelated(vararg days: DayRelated)

    @Delete
    abstract suspend fun deleteRozvrhDay(vararg days: RozvrhDay)

    @Transaction
    @Delete
    abstract suspend fun deleteDayRelated(vararg days: DayRelated)

    @Update
    abstract suspend fun updateRozvrhDay(vararg days: RozvrhDay)

    @Transaction
    @Update
    abstract suspend fun updateDayRelated(vararg days: DayRelated)

    @Query("SELECT * FROM rozvrhday WHERE date = :date")
    abstract suspend fun loadRozvrhDay(date: LocalDate): Flow<RozvrhDay>

    @Transaction
    @Query("SELECT * FROM rozvrhday WHERE date = :date")
    abstract suspend fun loadDayRelated(date: LocalDate): Flow<DayRelated>


}
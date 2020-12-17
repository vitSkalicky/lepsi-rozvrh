package cz.vitskalicky.lepsirozvrh.database

import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
abstract class RozvrhDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRozvrh(vararg rozvrhs: Rozvrh)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRozvrhRelated(vararg rozvrhs: RozvrhRelated)

    @Delete
    abstract suspend fun deleteRozvrh(vararg rozvrhs: Rozvrh)

    @Transaction
    @Delete
    abstract suspend fun deleteRozvrhRelated(vararg rozvrhs: RozvrhRelated)

    @Update
    abstract suspend fun updateRozvrh(vararg rozvrhs: Rozvrh)

    @Transaction
    @Update
    abstract suspend fun updateRozvrhRelated(vararg rozvrhs: RozvrhRelated)

    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract suspend fun loadRozvrh(monday: LocalDate): Flow<Rozvrh>

    @Transaction
    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract suspend fun loadRozvrhRelated(monday: LocalDate): Flow<RozvrhRelated>


}
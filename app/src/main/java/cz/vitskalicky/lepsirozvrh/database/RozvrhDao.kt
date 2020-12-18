package cz.vitskalicky.lepsirozvrh.database

import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@Dao
abstract class RozvrhDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRozvrh(vararg rozvrhs: Rozvrh)

    @Delete
    abstract suspend fun deleteRozvrh(vararg rozvrhs: Rozvrh)

    @Update
    abstract suspend fun updateRozvrh(vararg rozvrhs: Rozvrh)

    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract fun loadRozvrh(monday: LocalDate): Flow<Rozvrh>

    @Transaction
    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract fun loadRozvrhRelated(monday: LocalDate): Flow<RozvrhRelated>
}
package cz.vitskalicky.lepsirozvrh.database

import androidx.lifecycle.LiveData
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
    abstract fun loadRozvrhLive(monday: LocalDate): LiveData<Rozvrh>

    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract fun loadRozvrhRelatedLive(monday: LocalDate): LiveData<RozvrhRelated>

    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract suspend fun loadRozvrh(monday: LocalDate): Rozvrh?

    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract suspend fun loadRozvrhRelated(monday: LocalDate): RozvrhRelated?
}
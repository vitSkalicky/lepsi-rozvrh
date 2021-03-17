package cz.vitskalicky.lepsirozvrh.database

import androidx.lifecycle.LiveData
import androidx.room.*
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import org.joda.time.DateTime
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

    @Transaction
    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract fun loadRozvrhRelatedLive(monday: LocalDate): LiveData<RozvrhRelated>

    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract suspend fun loadRozvrh(monday: LocalDate): Rozvrh?

    @Transaction
    @Query("SELECT * FROM rozvrh WHERE id = :monday")
    abstract suspend fun loadRozvrhRelated(monday: LocalDate): RozvrhRelated?

    @Query("SELECT lastUpdate < :expireTime FROM rozvrh WHERE id= :monday")
    abstract suspend fun isExpired(monday: LocalDate, expireTime: DateTime): Boolean?

    @Query("DELETE FROM Rozvrh WHERE permanent = 0 AND id < :start OR id > :end")
    abstract fun deleteOutside(start: LocalDate, end: LocalDate)

    fun deleteUnnecessary(){
        deleteOutside(Utils.getCurrentMonday().minusWeeks(2), Utils.getCurrentMonday().plusWeeks(2))
    }

    @Query("UPDATE rozvrh SET lastUpdate = \"1980-10-12T00:00:00.042Z\" WHERE id != :monday")
    abstract fun invalidateAllOther(monday: LocalDate)
}
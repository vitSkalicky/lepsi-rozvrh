package cz.vitskalicky.lepsirozvrh.database

import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.relations.DayRelated
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@Dao
abstract class BlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRozvrhBlock(vararg blocks: RozvrhBlock)

    @Delete
    abstract suspend fun deleteRozvrhBlock(vararg blocks: RozvrhBlock)

    @Update
    abstract suspend fun updateRozvrhBlock(vararg blocks: RozvrhBlock)

    @Query("SELECT * FROM rozvrhblock WHERE day = :day AND caption == :caption")
    abstract fun loadRozvrhBlock(day: LocalDate, caption: String): Flow<RozvrhBlock>

    /*@Transaction
    @Query("SELECT * FROM rozvrhblock WHERE day = :day AND caption == :caption")
    abstract fun loadBlockRelated(day: LocalDate, caption: String): Flow<BlockRelated>*/


}
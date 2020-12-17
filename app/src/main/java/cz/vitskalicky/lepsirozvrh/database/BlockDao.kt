package cz.vitskalicky.lepsirozvrh.database

import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.relations.BlockRelated
import cz.vitskalicky.lepsirozvrh.model.relations.DayRelated
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
abstract class BlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRozvrhBlock(vararg blocks: RozvrhBlock)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBlockRelated(vararg blocks: BlockRelated)

    @Delete
    abstract suspend fun deleteRozvrhBlock(vararg blocks: RozvrhBlock)

    @Transaction
    @Delete
    abstract suspend fun deleteBlockRelated(vararg blocks: BlockRelated)

    @Update
    abstract suspend fun updateRozvrhBlock(vararg blocks: RozvrhBlock)

    @Transaction
    @Update
    abstract suspend fun updateBlockRelated(vararg blocks: BlockRelated)

    @Query("SELECT * FROM rozvrhblock WHERE day = :day AND caption == :caption")
    abstract suspend fun loadRozvrhBlock(day: LocalDate, caption: String): Flow<RozvrhBlock>

    @Transaction
    @Query("SELECT * FROM rozvrhblock WHERE day = :day AND caption == :caption")
    abstract suspend fun loadBlockRelated(day: LocalDate, caption: String): Flow<BlockRelated>


}
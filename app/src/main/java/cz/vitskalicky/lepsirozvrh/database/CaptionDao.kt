package cz.vitskalicky.lepsirozvrh.database

import androidx.room.*
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@Dao
abstract class CaptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCaption(vararg captions: RozvrhCaption)


    @Delete
    abstract suspend fun deleteCaption(vararg captions: RozvrhCaption)

    @Update
    abstract suspend fun updateCaption(vararg captions: RozvrhCaption)

    @Query("SELECT * FROM RozvrhCaption WHERE id = :id")
    abstract fun loadCaption(id: LocalDate): Flow<RozvrhCaption>


}
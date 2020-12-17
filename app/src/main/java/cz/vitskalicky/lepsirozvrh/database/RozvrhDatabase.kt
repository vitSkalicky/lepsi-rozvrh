package cz.vitskalicky.lepsirozvrh.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay

@Database(entities = arrayOf(Rozvrh::class, RozvrhBlock::class, RozvrhCaption::class, RozvrhDay::class), version = 1)
abstract class RozvrhDatabase : RoomDatabase() {
    abstract fun rozvrhDao(): RozvrhDao
    abstract fun BlockDao(): BlockDao
    abstract fun DayDao(): DayDao
    abstract fun CaptionDao(): CaptionDao
}

package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SchoolInfo.class}, version = 1, exportSchema = false)
public abstract class SchoolsDatabse extends RoomDatabase {
    public abstract SchoolDAO schoolDAO();
}

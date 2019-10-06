package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SchoolDAO {
    @Insert
    public void insertSchool(SchoolInfo school);

    @Insert
    public void insertSchools(List<SchoolInfo> schools);

    @Query("DELETE FROM schools")
    public void nukeTable();

    @Query("SELECT * FROM schools WHERE search_text MATCH :query")
    public DataSource.Factory<Integer, SchoolInfo> searchByName(String query);

    @Query("SELECT * FROM schools")
    public DataSource.Factory<Integer, SchoolInfo> queryAllSchools();

    @Query("SELECT count(*) FROM schools")
    public int countAllSchools();
}

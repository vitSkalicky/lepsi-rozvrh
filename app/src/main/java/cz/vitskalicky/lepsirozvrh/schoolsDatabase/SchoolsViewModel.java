package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.room.Room;

import java.text.Normalizer;
import java.util.List;

public class SchoolsViewModel extends ViewModel {
    public SchoolsViewModel() {
    }

    private SchoolsDatabse database = null;
    private SchoolDAO dao;
    private LiveData<PagedList<SchoolInfo>> allSchools;
    private LiveData<PagedList<SchoolInfo>> queriedSchools;
    private MutableLiveData<String> query = new MutableLiveData<>();

    public SchoolsDatabse init(Context context){
        context = context.getApplicationContext();
        if (database == null) {
            database = Room.inMemoryDatabaseBuilder(context, SchoolsDatabse.class).build();
            // /\ that one is right, \/ this is debug
            //database = Room.databaseBuilder(context, SchoolsDatabse.class,"schools-database.sqlite").build();
        }
        this.dao = database.schoolDAO();
        allSchools = new LivePagedListBuilder<>(dao.queryAllSchools(), 50).build();

        queriedSchools = Transformations.switchMap(query, v -> {
            System.out.println("search for: " + v);
            if (v.isEmpty()){
                return new LivePagedListBuilder<>(dao.queryAllSchools(), 50).build();
            }else {
                return new LivePagedListBuilder<>(dao.searchByName(v), 50).build();
            }
        });

        return database;
    }

    public LiveData<PagedList<SchoolInfo>> getAllSchools() {
        return allSchools;
    }

    public LiveData<PagedList<SchoolInfo>> getQueriedSchools() {
        return queriedSchools;
    }

    public void setQuery(String queryString){
        queryString = Normalizer.normalize(queryString, Normalizer.Form.NFD);
        queryString = queryString.replaceAll("[^\\p{ASCII}]", "");
        queryString = queryString.replace(" ", "* ");
        if (!queryString.isEmpty())
            queryString = queryString + "*";
        query.setValue(queryString);
    }

    public SchoolsDatabse getDatabase() {
        return database;
    }

    public SchoolDAO getDao() {
        return dao;
    }
}

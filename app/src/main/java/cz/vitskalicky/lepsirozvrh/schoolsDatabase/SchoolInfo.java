package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;

import java.text.Normalizer;

@Fts4
@Entity(tableName = "schools")
public class SchoolInfo {
    public String id;
    public String name;
    public String url;
    /**
     * only letters and digits from 'name', lowercase (accents are removed)
     */
    public String search_text;

    /**
     * Text is normalized and creates a phrase, which is used by full-text search
     */
    public void setSearchText(String name, String url) {
        search_text = makeSearchText(name,url);
    }

    private static String makeSearchText(String name, String url) {
        name = Normalizer.normalize(name, Normalizer.Form.NFD);
        name = name.replaceAll("[^\\p{ASCII}]", "");

        return name + " " + url;
    }

    @Override
    public int hashCode() {
        return id.hashCode() ^ name.hashCode() ^ url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SchoolInfo)) {
            return false;
        }
        SchoolInfo o = (SchoolInfo) obj;
        return id.equals(o.id) && name.equals(o.name) && url.equals(o.url);
    }

    @Override
    public String toString() {
        return String.format("%s (%s); %s; %s", name, search_text, url, id);
    }
}

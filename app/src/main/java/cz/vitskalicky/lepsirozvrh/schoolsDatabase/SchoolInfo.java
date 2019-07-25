package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

public class SchoolInfo {
    public String id;
    public String name;
    public String url;

    /**
     * only letters and digits from 'name', lowercase (accents are not removed)
     */
    public String stripedName;

    /**
     * Only lowercased letters and digits are copied to {@link #stripedName}.
     */
    public void createStripedName(String name){
        StringBuilder sb = new StringBuilder();
        for (char c :name.toCharArray()) {
            c = Character.toLowerCase(c);
            if (Character.isLetterOrDigit(c))
                sb.append(c);
        }
        stripedName = sb.toString();
    }

    @Override
    public int hashCode() {
        return id.hashCode() ^ name.hashCode() ^ url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SchoolInfo)){
            return false;
        }
        SchoolInfo o = (SchoolInfo) obj;
        return id.equals(o.id) && name.equals(o.name) && url.equals(o.url);
    }

    @Override
    public String toString() {
        return String.format("%s (%s); %s; %s", name, stripedName, url, id);
    }

    public static final Attribute<SchoolInfo, String> ID = new SimpleAttribute<SchoolInfo, String>() {
        @Override
        public String getValue(SchoolInfo object, QueryOptions queryOptions) { return object.id; }
    };

    public static final Attribute<SchoolInfo, String> NAME = new SimpleAttribute<SchoolInfo, String>() {
        @Override
        public String getValue(SchoolInfo object, QueryOptions queryOptions) { return object.name; }
    };

    public static final Attribute<SchoolInfo, String> URL = new SimpleAttribute<SchoolInfo, String>() {
        @Override
        public String getValue(SchoolInfo object, QueryOptions queryOptions) { return object.url; }
    };

    public static final Attribute<SchoolInfo, String> STRIPED_NAME = new SimpleAttribute<SchoolInfo, String>() {
        @Override
        public String getValue(SchoolInfo object, QueryOptions queryOptions) { return object.stripedName; }
    };
}

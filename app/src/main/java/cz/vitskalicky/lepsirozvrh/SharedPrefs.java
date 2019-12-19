package cz.vitskalicky.lepsirozvrh;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.toolbox.StringRequest;

/**
 * Utilities for shared preferences
 */
public class SharedPrefs {

    //key constants (do not change in future)
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD_HASH = "passwordHash";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String SENTRY_ID = "sentry_id";

    public static final String REMEMBERED_ROWS = "remembered_rows";
    public static final String REMEMBERED_COLUMNS = "remembered_columns";

    public static String getString(Context context, String key) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(key, "");
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences.Editor preferenceManager = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferenceManager.putString(key, value);
        preferenceManager.apply();
    }

    public static boolean getBoolean(Context context, String key) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(key, false);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor preferenceManager = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferenceManager.putBoolean(key, value);
        preferenceManager.apply();
    }

    public static int getInt(Context context, String key){
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(key, 0);
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences.Editor preferenceManager = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferenceManager.putInt(key, value);
        preferenceManager.apply();
    }

    public static void remove(Context context, String key) {
        SharedPreferences.Editor preferenceManager = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferenceManager.remove(key);
        preferenceManager.apply();
    }

    public static boolean contains(Context context, String key) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .contains(key);
    }


    public static boolean containsPreference(Context context, int stringId){
        return contains(context, context.getString(stringId));
    }

    public static String getStringPreference(Context context, int stringId, String defaultValue){
        if (!containsPreference(context, stringId)) return defaultValue;
        return getString(context, context.getString(stringId));
    }

    public static boolean getBooleanPreference(Context context, int stringId, boolean defaultValue){
        if (!containsPreference(context, stringId)) return defaultValue;

        return getBoolean(context, context.getString(stringId));
    }

    public static int getIntPreference(Context context, int stringId, int defaultValue){
        if (!containsPreference(context, stringId)) return defaultValue;
        return getInt(context, context.getString(stringId));
    }

    public static String getStringPreference(Context context, int stringId){
        return getStringPreference(context, stringId, "");
    }

    public static boolean getBooleanPreference(Context context, int stringId){
        return getBooleanPreference(context, stringId, false);
    }

    public static int getIntPreference(Context context, int stringId){
        return getIntPreference(context, stringId, 0);
    }

    public static void setStringPreference(Context context, int stringId, String value){
        setString(context, context.getString(stringId), value);
    }

    public static void setIntPreference(Context context, int stringId, int value){
        setInt(context, context.getString(stringId), value);
    }

    public static void setBooleanPreference(Context context, int stringId, boolean value){
        setBoolean(context, context.getString(stringId), value);
    }
}

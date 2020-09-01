package cz.vitskalicky.lepsirozvrh;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.android.volley.toolbox.StringRequest;

import org.joda.time.format.ISODateTimeFormat;

/**
 * Utilities for shared preferences
 */
public class SharedPrefs {

    //key constants (do not change in future)
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String ACCEESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    /**
     * ISO formatted date time on which access token expires.
     * @see ISODateTimeFormat#dateTime()
     */
    public static final String ACCESS_EXPIRES = "access_expires";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String SENTRY_ID = "sentry_id";

    public static final String REMEMBERED_ROWS = "remembered_rows";
    public static final String REMEMBERED_COLUMNS = "remembered_columns";

    public static final String LAST_VERSION_SEEN = "last_version_seen";
    /**
     * All weird rozvrh before this date won't show any dialog.
     */
    public static final String DISABLE_WTF_ROZVRH_UP_TO_DATE = "disable_wtf_rozvrh_up_to_date";

    /**
     * Access this one only using {@link AppSingleton#getWidgetsSettings()}.
     */
    public static final String WIDGETS_SETTINGS = "widgets-settings";

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

    public static float getFloat(Context context, String key){
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getFloat(key, 0);
    }

    public static void setFloat(Context context, String key, float value) {
        SharedPreferences.Editor preferenceManager = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferenceManager.putFloat(key, value);
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

    public static float getFloatPreference(Context context, int stringId, float defaultValue){
        if (!containsPreference(context, stringId)) return defaultValue;
        return getFloat(context, context.getString(stringId));
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

    public static void setFloatPreference(Context context, int stringId, float value){
        setFloat(context, context.getString(stringId), value);
    }

    public static void setBooleanPreference(Context context, int stringId, boolean value){
        setBoolean(context, context.getString(stringId), value);
    }
}

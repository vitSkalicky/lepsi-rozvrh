package cz.vitskalicky.lepsirozvrh;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utilities for shared preferences
 */
public class SharedPrefs {

    //key constants (do not change in future)
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD_HASH = "passwordHash";
    public static final String NAME = "name";

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

    public static void remove(Context context, String key){
        SharedPreferences.Editor preferenceManager = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferenceManager.remove(key);
        preferenceManager.apply();
    }

    public static boolean contains(Context context, String key){
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .contains(key);
    }
}

package cz.vitskalicky.lepsirozvrh.bakaAPI;

import android.content.Context;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import cz.vitskalicky.lepsirozvrh.SharedPrefs;

public class Token {
    private String token = "";
    private Calendar tokenDate;
    private final Context context;

    public Token(Context context) {
        this.context = context;
        tokenDate = Calendar.getInstance();
        tokenDate.clear();
        tokenDate.set(0,0,0);
    }

    /**
     * Calculates and returns token or empty string if there are no credentials in shared preferences.
     *
     * @return token or empty string if there are no credentials in shared preferences.
     */
    public static String getToken(Context context){

        if (!SharedPrefs.contains(context, SharedPrefs.USERNAME) || !SharedPrefs.contains(context, SharedPrefs.PASSWORD_HASH)){
            System.err.println("-!-!-!- Token requested and no saved credentials found! Please log in! -!-!-!-\n(returning empty token)");
            return "";
        }

        return Login.calculateToken(SharedPrefs.getString(context,SharedPrefs.USERNAME), SharedPrefs.getString(context, SharedPrefs.PASSWORD_HASH));
    }
}

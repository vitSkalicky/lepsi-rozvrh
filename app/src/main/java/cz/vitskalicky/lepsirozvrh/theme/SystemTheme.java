package cz.vitskalicky.lepsirozvrh.theme;

import android.content.Context;
import android.content.res.Configuration;

public class SystemTheme {

    public static boolean isDarkTheme(Context context){
        switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
                return false;
            default:
                return false;
        }
    }
}

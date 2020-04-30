package cz.vitskalicky.lepsirozvrh.activity;

import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.theme.Theme;

public class BaseActivity extends CyaneaAppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (Theme.of(this).checkSystemTheme()) {
            SharedPrefs.setBooleanPreference(this, R.string.THEME_CHANGED, true);
            recreate();
        }
    }
}

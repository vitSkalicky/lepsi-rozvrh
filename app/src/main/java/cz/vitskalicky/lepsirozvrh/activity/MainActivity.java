package cz.vitskalicky.lepsirozvrh.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.utils.ColorUtils;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.BuildConfig;
import cz.vitskalicky.lepsirozvrh.DebugUtils;
import cz.vitskalicky.lepsirozvrh.DisplayInfo;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.Login;
import cz.vitskalicky.lepsirozvrh.fragment.RozvrhFragment;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;
import cz.vitskalicky.lepsirozvrh.settings.SettingsActivity;
import cz.vitskalicky.lepsirozvrh.theme.Theme;
import cz.vitskalicky.lepsirozvrh.whatsnew.WhatsNewFragment;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_TIMER = TAG + "-timer";

    public static final String EXTRA_JUMP_TO_TODAY = MainActivity.class.getCanonicalName() + ".JUMP_TO_TODAY";
    private boolean showedNotiInfo = false;

    Context context = this;

    RozvrhFragment rFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLogin();

        rFragment = (RozvrhFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        int lastInterestingFeatureVersion = 16;
        String lastInterestingFeatureMessage = getString(R.string.interesting_themes);

        if (!SharedPrefs.contains(this, SharedPrefs.LAST_VERSION_SEEN) || (SharedPrefs.getInt(this, SharedPrefs.LAST_VERSION_SEEN) < lastInterestingFeatureVersion)) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.root), lastInterestingFeatureMessage, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.whats_new, view1 -> {
                WhatsNewFragment whatsNewFragment = new WhatsNewFragment();
                whatsNewFragment.show(getSupportFragmentManager(), "dialog");
            });
            snackbar.setActionTextColor(Cyanea.getInstance().getPrimary());
            snackbar.setDuration(5000);

            snackbar.show();

            SharedPrefs.setInt(this, SharedPrefs.LAST_VERSION_SEEN, BuildConfig.VERSION_CODE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ColorUtils.darker(Theme.of(this).getCHeaderBg()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SharedPrefs.getBooleanPreference(context, R.string.THEME_CHANGED, false)) {
            SharedPrefs.setBooleanPreference(context, R.string.THEME_CHANGED, false);
            recreate();
        }

        checkLogin();

        Intent intent = getIntent();
        boolean jumpToToday = intent.getBooleanExtra(EXTRA_JUMP_TO_TODAY, false);
        if (jumpToToday) {
            rFragment.jumpToWeek(0);
            intent.removeExtra(EXTRA_JUMP_TO_TODAY);
        } else {
            rFragment.setCenterToCurrentLesson(true);
            rFragment.jumpToWeek(0);
        }
        boolean fromNotification = intent.getBooleanExtra(PermanentNotification.INSTANCE.getEXTRA_NOTIFICATION(), false);
        intent.removeExtra(PermanentNotification.INSTANCE.getEXTRA_NOTIFICATION());
        if (fromNotification && !showedNotiInfo) {
            PermanentNotification.INSTANCE.showInfoDialog(context, false);
            showedNotiInfo = true;
        }

    }

    public void checkLogin() {
        Login login = ((MainApplication)getApplication()).getLogin();
        if (login.checkLogin(this) != null) {
            login.logout();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //todo RozvrhCache.clearOldCache(context);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}

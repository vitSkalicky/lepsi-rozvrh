package cz.vitskalicky.lepsirozvrh.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;

import cz.vitskalicky.lepsirozvrh.BuildConfig;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.Login;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button buttonStart = findViewById(R.id.buttonStart);
        CheckBox checkBox = findViewById(R.id.checkBoxSendCrashReports);
        Button buttonPrivacyPolicy = findViewById(R.id.buttonPrivacyPolicy);

        buttonStart.setOnClickListener(v -> {
            boolean sendReports = checkBox.isChecked();
            SharedPrefs.setInt(this, SharedPrefs.LAST_VERSION_SEEN, BuildConfig.VERSION_CODE);

            SharedPrefs.setBoolean(this, getString(R.string.PREFS_SEND_CRASH_REPORTS), sendReports);
            if (getApplication() instanceof MainApplication) {
                if (sendReports) {
                    ((MainApplication) getApplication()).enableSentry();
                } else {
                    ((MainApplication) getApplication()).diableSentry();
                }
            }
            Login.checkLogin(this);
            finish();
        });

        buttonPrivacyPolicy.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.PRIVACY_POLICY_LINK)));
            startActivity(browserIntent);
        });

        /*
         * Hide "send crash reports" checkbox on debug builds, because bug reports are allowed on
         * official release builds only. (see build.gradle)
         */
        if (!BuildConfig.ALLOW_SENTRY) {
            checkBox.setVisibility(View.GONE);
        }
    }
}

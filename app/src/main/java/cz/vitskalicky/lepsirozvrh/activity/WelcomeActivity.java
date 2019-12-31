package cz.vitskalicky.lepsirozvrh.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import cz.vitskalicky.lepsirozvrh.BuildConfig;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button buttonStart = findViewById(R.id.buttonStart);
        CheckBox checkBox = findViewById(R.id.checkBoxSendCrashReports);
        Button buttonPrivacyPolicy = findViewById(R.id.buttonPrivacyPolicy);

        buttonStart.setOnClickListener(v -> {
            boolean sendReports = checkBox.isChecked();
            if (!sendReports && BuildConfig.ALLOW_SENTRY) {
                //show confirmation dialog
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.scr_dialog)
                        .setMessage(R.string.scr_body)
                        .setPositiveButton(R.string.yes, (dialog1, which) -> onButtonPressed(true))
                        .setNegativeButton(R.string.no, (dialog1, which) -> onButtonPressed(false))
                        .create();
                dialog.show();
            } else {
                onButtonPressed(true);
            }
        });

        buttonPrivacyPolicy.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.PRIVACY_POLICY_LINK)));
            startActivity(browserIntent);
        });

        /*
         * Hide "send crash reports" checkbox on debug builds, because bug reports are allowed on
         * official release builds only. (see build.gradle)
         */
        if (!BuildConfig.ALLOW_SENTRY){
            checkBox.setVisibility(View.GONE);
        }
    }

    private void onButtonPressed(boolean sendReports) {
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
    }
}

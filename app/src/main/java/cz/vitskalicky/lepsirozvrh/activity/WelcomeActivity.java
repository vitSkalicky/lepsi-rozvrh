package cz.vitskalicky.lepsirozvrh.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

        buttonStart.setOnClickListener(v -> {
            boolean sendReports = checkBox.isChecked();
            if (!sendReports) {
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

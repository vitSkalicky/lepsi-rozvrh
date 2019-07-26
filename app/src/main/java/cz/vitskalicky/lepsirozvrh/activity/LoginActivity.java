package cz.vitskalicky.lepsirozvrh.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import javassist.bytecode.analysis.Util;

public class LoginActivity extends AppCompatActivity {
    /**
     * when you want this activity to log out as soon as it starts, pass this in intent extras.
     */
    public static final String LOGOUT = "logout";

    EditText etUsername;
    EditText etPassword;
    EditText etURL;
    Button bChooseSchool;
    Button bLogin;
    ProgressBar progressBar;
    TextView twMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.editTextName);
        etPassword = findViewById(R.id.editTextPassword);
        etURL = findViewById(R.id.editTextURL);
        bChooseSchool = findViewById(R.id.buttonSchoolList);
        bLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);
        twMessage = findViewById(R.id.textViewMessage);

        progressBar.setVisibility(View.GONE);
        twMessage.setText("");

        etUsername.setText(SharedPrefs.getString(this, SharedPrefs.USERNAME));
        etURL.setText(SharedPrefs.getString(this, SharedPrefs.URL));

        if (getIntent().getBooleanExtra(LOGOUT, false)){
            Login.logout(this);
        }

        bLogin.setOnClickListener(v -> {
            bLogin.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            twMessage.setText("");

            etUsername.getBackground().setColorFilter(null);
            etPassword.getBackground().setColorFilter(null);
            etURL.getBackground().setColorFilter(null);

            Login.login(etURL.getText().toString(), etUsername.getText().toString(), etPassword.getText().toString(), (code, data) -> {
                if (code == Login.SUCCESS){
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                bLogin.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (code == Login.WRONG_USERNAME){
                    twMessage.setText(R.string.invalid_username);
                    etUsername.getBackground().setColorFilter(getResources().getColor(R.color.colorError), PorterDuff.Mode.SRC_ATOP);
                }
                if (code == Login.WRONG_PASSWORD){
                    twMessage.setText(R.string.invalid_password);
                    etPassword.getBackground().setColorFilter(getResources().getColor(R.color.colorError), PorterDuff.Mode.SRC_ATOP);
                }
                if (code == Login.SERVER_UNREACHABLE){
                    twMessage.setText(R.string.unreachable);
                    etURL.getBackground().setColorFilter(getResources().getColor(R.color.colorError), PorterDuff.Mode.SRC_ATOP);
                }
                if (code == Login.UNEXPECTER_RESPONSE){
                    twMessage.setText(R.string.unexpected_response);
                    etURL.getBackground().setColorFilter(getResources().getColor(R.color.colorError), PorterDuff.Mode.SRC_ATOP);
                }
                if (code == Login.ROZVRH_DISABLED){
                    twMessage.setText(R.string.schedule_disabled);
                }

            }, this);
        });
    }
}

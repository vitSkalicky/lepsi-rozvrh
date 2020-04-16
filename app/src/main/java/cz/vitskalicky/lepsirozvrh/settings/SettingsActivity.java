package cz.vitskalicky.lepsirozvrh.settings;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;

public class SettingsActivity extends CyaneaAppCompatActivity {
    Toolbar toolbar;
    SettingsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Cyanea.getInstance().getMenuIconColor());
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        fragment.setLogoutListener(() -> {
            Login.logout(this);
            /*Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(LoginActivity.LOGOUT, true);
            startActivity(intent);*/
            finish();
            return;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

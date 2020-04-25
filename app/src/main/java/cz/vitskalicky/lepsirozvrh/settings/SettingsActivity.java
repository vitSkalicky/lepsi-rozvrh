package cz.vitskalicky.lepsirozvrh.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;

public class SettingsActivity extends CyaneaAppCompatActivity implements Utils.RecreateWithAnimationActivity {

    Toolbar toolbar;
    SettingsFragment settingsFragment;
    ThemeSettingsFragment themeSettingsFragment;
    ExportThemeFragment exportThemeFragment;
    ImportThemeFragment importThemeFragment;
    View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().hasExtra("bundle") && savedInstanceState == null) {
            savedInstanceState = getIntent().getExtras().getBundle("bundle");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Cyanea.getInstance().getMenuIconColor());
        toolbar.setBackgroundColor(Cyanea.getInstance().getPrimary());
        setSupportActionBar(toolbar);

        root = findViewById(R.id.root);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            settingsFragment = (SettingsFragment) fm.getFragment(savedInstanceState, "settingsFragment");
            themeSettingsFragment = (ThemeSettingsFragment) fm.getFragment(savedInstanceState, "themeSettingsFragment");
            exportThemeFragment = (ExportThemeFragment) fm.getFragment(savedInstanceState, "exportThemeFragment");
            importThemeFragment = (ImportThemeFragment) fm.getFragment(savedInstanceState, "importThemeFragment");
            setupRootListeners();
            setupThemeListeners();
        }

        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
            setupRootListeners();
            fm.beginTransaction()
                    .replace(R.id.frame_layout, settingsFragment, "settingsFragment")
                    .commit();
        }
    }


    private void setupRootListeners() {
        if (settingsFragment != null) {
            settingsFragment.setLogoutListener(() -> {
                Login.logout(this);
                finish();
                return;
            });
            settingsFragment.setShownThemeSettingsListener(() -> {
                if (themeSettingsFragment == null) {
                    themeSettingsFragment = new ThemeSettingsFragment();
                    setupThemeListeners();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, themeSettingsFragment, "themeSettingsFragment")
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void setupThemeListeners() {
        if (themeSettingsFragment != null) {
            themeSettingsFragment.setExportListener(() -> {
                if (exportThemeFragment == null) {
                    exportThemeFragment = new ExportThemeFragment();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, exportThemeFragment, "exportThemeFragment")
                        .addToBackStack(null)
                        .commit();
            });
            themeSettingsFragment.setImportListener(() -> {
                if (importThemeFragment == null) {
                    importThemeFragment = new ImportThemeFragment();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, importThemeFragment, "importThemeFragment")
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    @Override
    public void recreateWithAnimation() {
        Bundle temp_bundle = new Bundle();
        onSaveInstanceState(temp_bundle);
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("bundle", temp_bundle);
        finish();
        startActivity(intent);
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (settingsFragment != null && getSupportFragmentManager().findFragmentByTag("settingsFragment") != null)
            getSupportFragmentManager().putFragment(outState, "settingsFragment", settingsFragment);
        if (themeSettingsFragment != null && getSupportFragmentManager().findFragmentByTag("themeSettingsFragment") != null)
            getSupportFragmentManager().putFragment(outState, "themeSettingsFragment", themeSettingsFragment);
        if (exportThemeFragment != null && getSupportFragmentManager().findFragmentByTag("exportThemeFragment") != null)
            getSupportFragmentManager().putFragment(outState, "exportThemeFragment", exportThemeFragment);
        if (importThemeFragment != null && getSupportFragmentManager().findFragmentByTag("importThemeFragment") != null)
            getSupportFragmentManager().putFragment(outState, "importThemeFragment", importThemeFragment);
    }
}

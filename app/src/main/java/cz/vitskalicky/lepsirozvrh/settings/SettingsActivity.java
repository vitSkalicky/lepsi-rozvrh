package cz.vitskalicky.lepsirozvrh.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.jaredrummler.cyanea.Cyanea;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.activity.BaseActivity;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.donations.Donations;

public class SettingsActivity extends BaseActivity implements Utils.RecreateWithAnimationActivity {

    private Toolbar toolbar;
    private SettingsFragment settingsFragment;
    private ThemeSettingsFragment themeSettingsFragment;
    private ExportThemeFragment exportThemeFragment;
    private ImportThemeFragment importThemeFragment;
    private View root;

    private Donations donations;

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

        donations = new Donations(this,this, this::sponsorChanged);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            settingsFragment = (SettingsFragment) fm.getFragment(savedInstanceState, "settingsFragment");
            themeSettingsFragment = (ThemeSettingsFragment) fm.getFragment(savedInstanceState, "themeSettingsFragment");
            exportThemeFragment = (ExportThemeFragment) fm.getFragment(savedInstanceState, "exportThemeFragment");
            importThemeFragment = (ImportThemeFragment) fm.getFragment(savedInstanceState, "importThemeFragment");
            setupRootListeners();
            setupThemeListeners();
            if (importThemeFragment != null){
                importThemeFragment.init(donations);
            }
        }

        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
            setupRootListeners();
            fm.beginTransaction()
                    .replace(R.id.frame_layout, settingsFragment, "settingsFragment")
                    .commit();
        }
        settingsFragment.setSupportingEnabled(donations.isEnabled());
        settingsFragment.setSponsor(donations.isSponsor());

        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if (!intent.getBooleanExtra("ignore",false)){
            String dataString = intent.getDataString();
            if (dataString != null){
                String data = "";
                if (dataString.startsWith("https://vitskalicky.github.io/lepsi-rozvrh/motiv")){
                    Uri uri = Uri.parse(dataString);
                    data = uri.getQueryParameter("data");
                } else if (dataString.startsWith("lepsi-rozvrh:motiv/")){
                    data = dataString.substring(19);
                }
                clearBackstack();
                showThemeSettings();
                showImportFragment();
                if (!donations.isSponsor()){
                    donations.showDialog();
                }
                importThemeFragment.setString(data);
                intent.putExtra("ignore",true);
            }
        }
    }

    public void clearBackstack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(
                    0);
            getSupportFragmentManager().popBackStack(entry.getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private void showThemeSettings(){
        if (themeSettingsFragment == null) {
            themeSettingsFragment = new ThemeSettingsFragment();
            setupThemeListeners();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, themeSettingsFragment, "themeSettingsFragment")
                .addToBackStack(null)
                .commit();
    }

    private void showImportFragment(){
        if (importThemeFragment == null) {
            importThemeFragment = new ImportThemeFragment();
            importThemeFragment.init(donations);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, importThemeFragment, "importThemeFragment")
                .addToBackStack(null)
                .commit();
    }

    private void showExportFragment(){
        if (exportThemeFragment == null) {
            exportThemeFragment = new ExportThemeFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, exportThemeFragment, "exportThemeFragment")
                .addToBackStack(null)
                .commit();
    }

    private void setupRootListeners() {
        if (settingsFragment != null) {
            settingsFragment.init(donations);
            settingsFragment.setLogoutListener(() -> {
                Login.logout(this);
                finish();
                return;
            });
            settingsFragment.setShownThemeSettingsListener(this::showThemeSettings);
        }
    }

    private void setupThemeListeners() {
        if (themeSettingsFragment != null) {
            themeSettingsFragment.setExportListener(this::showExportFragment);
            themeSettingsFragment.setImportListener(this::showImportFragment);
            themeSettingsFragment.init(donations);
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

    public void sponsorChanged() {
        if (settingsFragment != null){
            settingsFragment.setSponsor(donations.isSponsor());
        }
        if (themeSettingsFragment != null){
            themeSettingsFragment.updateDonationEnability();
        }
        if (importThemeFragment != null){
            importThemeFragment.updateDonationsStatus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        donations.release();
    }
}

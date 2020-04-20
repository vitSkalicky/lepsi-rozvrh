package cz.vitskalicky.lepsirozvrh.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.theme.Theme;

public class SettingsActivity extends CyaneaAppCompatActivity {
    private static final int SAVE_CODE = 324;
    private static final int LOAD_CODE = 8723;

    Toolbar toolbar;
    SettingsFragment settingsFragment;
    ThemeSettingsFragment themeSettingsFragment;
    View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().hasExtra("bundle") && savedInstanceState==null){
            savedInstanceState = getIntent().getExtras().getBundle("bundle");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Cyanea.getInstance().getMenuIconColor());
        setSupportActionBar(toolbar);

        root = findViewById(R.id.root);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            settingsFragment = (SettingsFragment) fm.getFragment(savedInstanceState, "settingsFragment");
            themeSettingsFragment = (ThemeSettingsFragment) fm.getFragment(savedInstanceState, "themeSettingsFragment");
            setupRootListeners();
            setupThemeListeners();
        }

        if (settingsFragment == null){
            settingsFragment = new SettingsFragment();
            setupRootListeners();
            fm.beginTransaction()
                    .replace(R.id.frame_layout, settingsFragment,"settingsFragment")
                    .commit();
        }
    }



    private void setupRootListeners(){
        if (settingsFragment != null){
            settingsFragment.setLogoutListener(() -> {
                Login.logout(this);
                finish();
                return;
            });
            settingsFragment.setShownThemeSettingsListener(() -> {
                if (themeSettingsFragment == null){
                    themeSettingsFragment = new ThemeSettingsFragment();
                    setupThemeListeners();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, themeSettingsFragment,"themeSettingsFragment")
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void setupThemeListeners(){
        if (themeSettingsFragment != null){
            themeSettingsFragment.setRecreateListener(() -> {
                Bundle temp_bundle = new Bundle();
                onSaveInstanceState(temp_bundle);
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("bundle", temp_bundle);
                finish();
                startActivity(intent);
                overridePendingTransition(0, android.R.anim.fade_out);
            });
            themeSettingsFragment.setExportListener(() -> {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/json");
                intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.theme_file_name) + ".bstheme");

                startActivityForResult(intent, SAVE_CODE);
            });
            themeSettingsFragment.setImportListener(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, LOAD_CODE);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == SAVE_CODE){
                AsyncTask.execute(() -> {
                    if (data != null){
                        Uri contentUri = data.getData();
                        try {
                            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(contentUri, "w");
                            FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());

                            Theme theme = new Theme(this);
                            Theme.writeThemeData(fos, theme.getThemeData());

                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.somethingWrong(e, root, this);
                        }
                    }
                });

            }

            if (requestCode == LOAD_CODE){
                AsyncTask.execute(() -> {
                    if (data != null) {
                        Uri uri = data.getData();

                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            Theme theme = new Theme(this);
                            theme.setThemeData(Theme.loadThemeData(inputStream));
                            new Handler(getMainLooper()).post(() -> {
                                if (themeSettingsFragment != null){
                                    themeSettingsFragment.applyChanges();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            Snackbar.make(root, R.string.malformed_theme_file, BaseTransientBottomBar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });

            }
        }
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
        if (settingsFragment != null)
            getSupportFragmentManager().putFragment(outState, "settingsFragment", settingsFragment);
        if (themeSettingsFragment != null)
            getSupportFragmentManager().putFragment(outState, "themeSettingsFragment", themeSettingsFragment);
    }
}

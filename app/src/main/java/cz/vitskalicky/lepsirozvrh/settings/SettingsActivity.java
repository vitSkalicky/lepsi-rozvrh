package cz.vitskalicky.lepsirozvrh.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.theme.Themator;

public class SettingsActivity extends CyaneaAppCompatActivity {
    Toolbar toolbar;
    SettingsFragment fragment;

    private static final int SAVE_CODE = 324;
    private static final int LOAD_CODE = 8723;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        fragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment.setLogoutListener(() -> {
            Login.logout(this);
            /*Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(LoginActivity.LOGOUT, true);
            startActivity(intent);*/
            finish();
            return;
        });

        fragment.setOnThemeClickListener( () -> {
            // Instantiate the new Fragment
            final ThemeSettingsFragment themeFragment = new ThemeSettingsFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, themeFragment)
                    .addToBackStack(null)
                    .commit();
            themeFragment.setOnSaveClickedListener(() -> {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, "better schedule theme.json");

                startActivityForResult(intent, SAVE_CODE);
            });
            themeFragment.setOnLoadClickedListener(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/plain");
                startActivityForResult(intent, LOAD_CODE);
            });
        });

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

                            Themator themator = new Themator(this);
                            boolean ok = themator.writeCurrentTheme(fos);

                            if (!ok){
                                Toast.makeText(this, "Error",Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error",Toast.LENGTH_LONG).show();
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
                            Themator themator = new Themator(this);
                            boolean ok = themator.loadTheme(inputStream);
                            if (!ok){
                                Toast.makeText(this, "Error",Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error",Toast.LENGTH_LONG).show();
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
}

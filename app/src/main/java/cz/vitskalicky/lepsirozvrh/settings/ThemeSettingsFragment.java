package cz.vitskalicky.lepsirozvrh.settings;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.jaredrummler.cyanea.Cyanea;

import java.io.IOException;
import java.io.InputStream;
import java.time.temporal.ValueRange;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.theme.Themator;

public class ThemeSettingsFragment extends PreferenceFragmentCompat {

    private PreferenceCategory save_load;
    private PreferenceCategory empty;
    private PreferenceCategory normal;
    private PreferenceCategory change;
    private PreferenceCategory no_school;
    private PreferenceCategory headers;
    private PreferenceCategory dividers;
    private PreferenceCategory infoline;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);

        save_load = findPreference("save_load");
        empty = findPreference("empty");
        normal = findPreference("normal");
        change = findPreference("change");
        no_school = findPreference("no_school");
        headers = findPreference("headers");
        dividers = findPreference("dividers");
        infoline = findPreference("infoline");

        int theme;
        try{
            theme = Integer.parseInt((String) SharedPrefs.getString(getContext(), "theme"));
        }catch (RuntimeException e){
            theme = 0;
        }
        if (theme < 3){
            save_load.setVisible(false);
            empty.setVisible(false);
            normal.setVisible(false);
            change.setVisible(false);
            no_school.setVisible(false);
            headers.setVisible(false);
            dividers.setVisible(false);
            infoline.setVisible(false);
        }else{
            save_load.setVisible(true);
            empty.setVisible(true);
            normal.setVisible(true);
            change.setVisible(true);
            no_school.setVisible(true);
            headers.setVisible(true);
            dividers.setVisible(true);
            infoline.setVisible(true);
        }


        findPreference("divider width").setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                float value = Float.parseFloat((String) newValue);
                Themator themator = new Themator(getContext());
                themator.setRozvrhDividerWidth(value);
            }catch (NumberFormatException e){
                Toast.makeText(getContext(), "Enter a decimal number (e.g. 0.8)", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        });
        findPreference("highlight width").setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                float value = Float.parseFloat((String) newValue);
                Themator themator = new Themator(getContext());
                themator.setRozvrhHighlightWidth(value);
            }catch (NumberFormatException e){
                Toast.makeText(getContext(), "Enter a decimal number (e.g. 0.8)", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        });
        findPreference("infoline text size").setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                float value = Float.parseFloat((String) newValue);
                Themator themator = new Themator(getContext());
                themator.setRozvrhHighlightWidth(value);
            }catch (NumberFormatException e){
                Toast.makeText(getContext(), "Enter a decimal number (e.g. 0.8)", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        });

        findPreference("save").setOnPreferenceClickListener(preference -> {
            onSaveClickedListener.method();
            return true;
        });
        findPreference("load").setOnPreferenceClickListener(preference -> {
            onLoadClickedListener.method();
            return true;
        });
        findPreference("default").setOnPreferenceClickListener(preference -> {
            Themator themator = new Themator(getContext());
            themator.applyDefaultTheme();
            return true;
        });

        findPreference("theme").setOnPreferenceChangeListener((preference, newValue) -> {
            int value = Integer.parseInt((String) newValue);
            if (value < 3){
                int resId;
                switch (value){
                    case 0:
                        //noinspection DuplicateBranchesInSwitch
                        resId = R.raw.theme_light;
                        break;
                    case 1:
                        resId = R.raw.theme_dark;
                        break;
                    case 2:
                        resId = R.raw.theme_black;
                        break;
                    default:
                        //fail safe
                        resId = R.raw.theme_light;
                }
                AsyncTask.execute(() -> {
                    try (InputStream is = getResources().openRawResource(resId);){
                        Themator themator = new Themator(getContext());
                        themator.loadTheme(is);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                save_load.setVisible(false);
                empty.setVisible(false);
                normal.setVisible(false);
                change.setVisible(false);
                no_school.setVisible(false);
                headers.setVisible(false);
                dividers.setVisible(false);
                infoline.setVisible(false);
            }else{
                save_load.setVisible(true);
                empty.setVisible(true);
                normal.setVisible(true);
                change.setVisible(true);
                no_school.setVisible(true);
                headers.setVisible(true);
                dividers.setVisible(true);
                infoline.setVisible(true);
            }
            applyChanges();
            return true;
        });
    }

    private Utils.Listener onSaveClickedListener = () -> {};
    private Utils.Listener onLoadClickedListener = () -> {};

    public void setOnSaveClickedListener(Utils.Listener onSaveClickedListener) {
        this.onSaveClickedListener = onSaveClickedListener;
    }

    public void setOnLoadClickedListener(Utils.Listener onLoadClickedListener) {
        this.onLoadClickedListener = onLoadClickedListener;
    }

    private void applyChanges(){
        if (getActivity() instanceof SettingsActivity){
            SettingsActivity activity = (SettingsActivity) getActivity();
            SharedPrefs.setBoolean(getContext(), "theme_changed", true);
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            intent.putExtra("start_theme", true);
            startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            activity.finish();
        }
    }


}

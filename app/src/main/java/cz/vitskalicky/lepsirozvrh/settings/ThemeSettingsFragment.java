package cz.vitskalicky.lepsirozvrh.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.theme.Themator;

public class ThemeSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);

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
    }

    private Utils.Listener onSaveClickedListener = () -> {};
    private Utils.Listener onLoadClickedListener = () -> {};

    public void setOnSaveClickedListener(Utils.Listener onSaveClickedListener) {
        this.onSaveClickedListener = onSaveClickedListener;
    }

    public void setOnLoadClickedListener(Utils.Listener onLoadClickedListener) {
        this.onLoadClickedListener = onLoadClickedListener;
    }
}

package cz.vitskalicky.lepsirozvrh.settings;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;
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
    private PreferenceCategory basic;
    private PreferenceCategory empty;
    private PreferenceCategory normal;
    private PreferenceCategory change;
    private PreferenceCategory no_school;
    private PreferenceCategory headers;
    private PreferenceCategory dividers;
    private PreferenceCategory infoline;

    private Preference more;
    private Preference less;

    private ColorPreferenceCompat primary;
    private ColorPreferenceCompat accent;
    private ColorPreferenceCompat background;
    private EditTextPreference primary_text;
    private EditTextPreference secondary_text;

    private ColorPreferenceCompat emptyBg;

    private ColorPreferenceCompat hodinaBg;
    private ColorPreferenceCompat hodinaPrimary;
    private ColorPreferenceCompat hodinaSecondary;
    private ColorPreferenceCompat hodinaRoom;

    private ColorPreferenceCompat changeBg;
    private ColorPreferenceCompat changePrimary;
    private ColorPreferenceCompat changeSecondary;
    private ColorPreferenceCompat changeRoom;

    private ColorPreferenceCompat aBg;
    private ColorPreferenceCompat aPrimary;
    private ColorPreferenceCompat aSecondary;
    private ColorPreferenceCompat aRoom;

    private ColorPreferenceCompat headerBg;
    private ColorPreferenceCompat headerPrimary;
    private ColorPreferenceCompat headerSecondary;

    private ColorPreferenceCompat divider;
    private EditTextPreference divider_width;
    private ColorPreferenceCompat highlight;
    private EditTextPreference highlight_width;

    private ColorPreferenceCompat infoline_color;
    private ColorPreferenceCompat infoline_text;
    private EditTextPreference infoline_text_size;

    /**
     * 0 = none
     * 1 = load, save, primary,...
     * 2 = cell colors, divider,...
     * 3 = texts colors
     */
    private int detailLevel = 0;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);
        Themator themator = new Themator(getContext());
        Cyanea cyanea = Cyanea.getInstance();

        detailLevel = SharedPrefs.getInt(getContext(), "themeDetailLevel");

        save_load = findPreference("save_load");
        basic = findPreference("basic");
        empty = findPreference("empty");
        normal = findPreference("normal");
        change = findPreference("change");
        no_school = findPreference("no_school");
        headers = findPreference("headers");
        dividers = findPreference("dividers");
        infoline = findPreference("infoline");

        primary = findPreference("pref_color_primary");
        accent = findPreference("pref_color_accent");
        background = findPreference("pref_color_background");

        more = findPreference("more");
        less = findPreference("less");

        more = findPreference("more");
        less = findPreference("less");
        primary = findPreference("pref_color_primary");
        accent = findPreference("pref_color_accent");
        background = findPreference("pref_color_background");
        primary_text = findPreference("primary_text");
        secondary_text = findPreference("secondary_text");
        emptyBg = findPreference(getString(R.string.THEME_ROZVRH_COLOR_BG_EMPTY));
        hodinaBg = findPreference(getString(R.string.THEME_ROZVRH_COLOR_BG_H));
        hodinaPrimary = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_PRIMARY_TEXT));
        hodinaSecondary = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_SECONDARY_TEXT));
        hodinaRoom = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_ROOM_TEXT));
        changeBg = findPreference(getString(R.string.THEME_ROZVRH_COLOR_BG_CHNG));
        changePrimary = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_PRIMARY_TEXT));
        changeSecondary = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_SECONDARY_TEXT));
        changeRoom = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_ROOM_TEXT));
        aBg = findPreference(getString(R.string.THEME_ROZVRH_COLOR_BG_A));
        aPrimary = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_A_PRIMARY_TEXT));
        aSecondary = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_A_SECONDARY_TEXT));
        aRoom = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HODINA_A_ROOM_TEXT));
        headerBg = findPreference(getString(R.string.THEME_ROZVRH_COLOR_BG_HEADER));
        headerPrimary = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HEADER_PRIMARY_TEXT));
        headerSecondary = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HEADER_SECONDARY_TEXT));
        divider = findPreference(getString(R.string.THEME_ROZVRH_COLOR_DIVIDER));
        divider_width = findPreference("divider width");
        highlight = findPreference(getString(R.string.THEME_ROZVRH_COLOR_HIGHLIGHT));
        highlight_width = findPreference("highlight width");
        infoline_color = findPreference(getString(R.string.THEME_INFOLINE_COLOR));
        infoline_text = findPreference(getString(R.string.THEME_INFOLINE_COLOR_TEXT));
        infoline_text_size = findPreference("infoline text size");


        int theme;
        try{
            theme = Integer.parseInt((String) SharedPrefs.getString(getContext(), "theme"));
        }catch (RuntimeException e){
            theme = 0;
        }
        if (theme < 3){
            save_load.setVisible(false);
            basic.setVisible(false);
            empty.setVisible(false);
            normal.setVisible(false);
            change.setVisible(false);
            no_school.setVisible(false);
            headers.setVisible(false);
            dividers.setVisible(false);
            infoline.setVisible(false);
        }else{
            save_load.setVisible(true);
            basic.setVisible(true);
            empty.setVisible(true);
            normal.setVisible(true);
            change.setVisible(true);
            no_school.setVisible(true);
            headers.setVisible(true);
            dividers.setVisible(true);
            infoline.setVisible(true);
        }


        divider_width.setDefaultValue(themator.getRozvrhDividerWidth());
        divider_width.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                float value = Float.parseFloat((String) newValue);
                themator.setRozvrhDividerWidth(value);
            }catch (NumberFormatException e){
                Toast.makeText(getContext(), "Enter a decimal number (e.g. 0.8)", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        });
        highlight_width.setDefaultValue(themator.getRozvrhHighlightWidth());
        highlight_width.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                float value = Float.parseFloat((String) newValue);
                themator.setRozvrhHighlightWidth(value);
            }catch (NumberFormatException e){
                Toast.makeText(getContext(), "Enter a decimal number (e.g. 0.8)", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        });
        infoline_text_size.setDefaultValue(themator.getRozvrhHighlightWidth());
        infoline_text_size.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                float value = Float.parseFloat((String) newValue);
                themator.setRozvrhHighlightWidth(value);
            }catch (NumberFormatException e){
                Toast.makeText(getContext(), "Enter a decimal number (e.g. 0.8)", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        });
        primary_text.setDefaultValue(themator.getRozvrhPrimaryTextSize());
        primary_text.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                float value = Float.parseFloat((String) newValue);
                themator.setRozvrhPrimaryTextSize(value);
            }catch (NumberFormatException e){
                Toast.makeText(getContext(), "Enter a decimal number (e.g. 0.8)", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        });
        secondary_text.setDefaultValue(themator.getRozvrhSecondaryTextSize());
        secondary_text.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                float value = Float.parseFloat((String) newValue);
                themator.setRozvrhSecondaryTextSize(value);
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
                        themator.loadTheme(is);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setDetailLevel(0);
            }else{
                setDetailLevel(2);
            }
            applyChanges();
            return true;
        });

        more.setOnPreferenceClickListener(preference -> {
            setDetailLevel(detailLevel + 1);
            return true;
        });
        less.setOnPreferenceClickListener(preference -> {
            setDetailLevel(detailLevel - 1);
            themator.regenerateColors(detailLevel);
            return true;
        });

        SharedPrefs.setInt(getContext(), "pref_color_primary", cyanea.getPrimary());
        primary.setDefaultValue(cyanea.getPrimary());
        primary.setOnPreferenceChangeListener((preference, newValue) -> {
            cyanea.edit().primary((Integer) newValue).apply();
            themator.regenerateColors(detailLevel);
            applyChanges();
            return true;
        });
        SharedPrefs.setInt(getContext(), "pref_color_accent", cyanea.getAccent());
        accent.setDefaultValue(cyanea.getPrimary());
        accent.setOnPreferenceChangeListener((preference, newValue) -> {
            cyanea.edit().accent((Integer) newValue).apply();
            themator.regenerateColors(detailLevel);
            applyChanges();
            return true;
        });
        SharedPrefs.setInt(getContext(), "pref_color_background", cyanea.getBackgroundColor());
        background.setDefaultValue(cyanea.getBackgroundColor());
        background.setOnPreferenceChangeListener((preference, newValue) -> {
            cyanea.edit().background((Integer) newValue).apply();
            themator.regenerateColors(detailLevel);
            applyChanges();
            return true;
        });

        updateVisibility();
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

    private void setDetailLevel(int detailLevel){
        this.detailLevel = detailLevel;
        SharedPrefs.setInt(getContext(), "themeDetailLevel", detailLevel);
        updateVisibility();
    }

    private void updateVisibility(){
        if (detailLevel > 0){
            save_load.setVisible(true);
            basic.setVisible(true);
        }else {
            save_load.setVisible(false);
            basic.setVisible(false);
        }
        if (detailLevel > 1){
            empty.setVisible(true);
            normal.setVisible(true);
            change.setVisible(true);
            no_school.setVisible(true);
            headers.setVisible(true);
            dividers.setVisible(true);
            infoline.setVisible(true);
        } else{
            empty.setVisible(false);
            normal.setVisible(false);
            change.setVisible(false);
            no_school.setVisible(false);
            headers.setVisible(false);
            dividers.setVisible(false);
            infoline.setVisible(false);
        }
        if (detailLevel > 2){
            primary_text.setVisible(true);
            secondary_text.setVisible(true);
            hodinaPrimary.setVisible(true);
            hodinaSecondary.setVisible(true);
            hodinaRoom.setVisible(true);
            changePrimary.setVisible(true);
            changeSecondary.setVisible(true);
            changeRoom.setVisible(true);
            aPrimary.setVisible(true);
            aSecondary.setVisible(true);
            aRoom.setVisible(true);
            headerPrimary.setVisible(true);
            headerSecondary.setVisible(true);
            infoline_text.setVisible(true);
            infoline_text_size.setVisible(true);
        } else {
            primary_text.setVisible(false);
            secondary_text.setVisible(false);
            hodinaPrimary.setVisible(false);
            hodinaSecondary.setVisible(false);
            hodinaRoom.setVisible(false);
            changePrimary.setVisible(false);
            changeSecondary.setVisible(false);
            changeRoom.setVisible(false);
            aPrimary.setVisible(false);
            aSecondary.setVisible(false);
            aRoom.setVisible(false);
            headerPrimary.setVisible(false);
            headerSecondary.setVisible(false);
            infoline_text.setVisible(false);
            infoline_text_size.setVisible(false);
        }
        if (detailLevel > 0 && detailLevel < 3){
            more.setVisible(true);
        }else{
            more.setVisible(false);
        }
        if (detailLevel > 1){
            less.setVisible(true);
        }else {
            less.setVisible(false);
        }
    }


    @Override
    public void onPause() {
        Themator themator = new Themator(getContext());
        themator.regenerateColors(detailLevel);
        super.onPause();
    }
}

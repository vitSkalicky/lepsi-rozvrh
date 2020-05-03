package cz.vitskalicky.lepsirozvrh.widget;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.preference.DropDownPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.theme.Theme;

/**
 * A simple {@link Fragment} subclass.
 */
public class WidgetThemeFragment extends PreferenceFragmentCompat {

    private DropDownPreference themeP;
    private ColorPreferenceCompat backgroundP;
    private SeekBarPreference transparencyP;
    private SwitchPreferenceCompat autotextP;
    private ColorPreferenceCompat textColorP;

    private int theme;
    private int background;
    private int transparency;
    private int text;
    private boolean autotext;

    private int widgetID;
    private boolean isWidgetIDSet = false;

    private CallbackListener callbackListener = new CallbackListener() {
        @Override
        public void setBackground(int color) {

        }

        @Override
        public void setPrimaryTextColor(int color) {

        }

        @Override
        public void setSecondaryTextColor(int color) {

        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (savedInstanceState != null) {
            theme = savedInstanceState.getInt("theme", 0);
            background = savedInstanceState.getInt("background", ContextCompat.getColor(getContext(), R.color.widgetLightBackground));
            transparency = savedInstanceState.getInt("transparency", 0);
            autotext = savedInstanceState.getBoolean("autotext", true);
            if (autotext) {
                text = generateTextColor();
            } else {
                text = savedInstanceState.getInt("text", generateTextColor());
            }
        } else {
            theme = 0;
            background = ContextCompat.getColor(getContext(), R.color.widgetLightBackground);
            transparency = 255;
            text = generateTextColor();
            autotext = true;
        }
        setPreferencesFromResource(R.xml.widget_preferences, rootKey);
        themeP = findPreference(getString(R.string.WIDGET_PREF_THEME));
        backgroundP = findPreference(getString(R.string.WIDGET_PREF_BG));
        transparencyP = findPreference(getString(R.string.WIDGET_PREF_TRANSPARENCY));
        autotextP = findPreference(getString(R.string.WIDGET_PREF_AUTO_TEXT));
        textColorP = findPreference(getString(R.string.WIDGET_PREF_TEXT));

        themeP.setOnPreferenceChangeListener((preference, newValue) -> {
            theme = Integer.parseInt(newValue.toString());
            updateTheme();
            return true;
        });
        themeP.setSummaryProvider(preference -> ((DropDownPreference) preference).getEntry());

        backgroundP.setOnPreferenceChangeListener((preference, newValue) -> {
            background = (int) newValue;
            callbackListener.setBackground(getBackgroundWithAlpha());
            if (autotext) {
                text = generateTextColor();
                textColorP.saveValue(text);
                callbackListener.setPrimaryTextColor(text);
                callbackListener.setSecondaryTextColor(getSecondaryTextColor());
            }
            return true;
        });
        transparencyP.setUpdatesContinuously(true);
        transparencyP.setOnPreferenceChangeListener((preference, newValue) -> {
            transparency = (int) newValue;
            callbackListener.setBackground(getBackgroundWithAlpha());
            return true;
        });
        autotextP.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean value = (boolean) newValue;
            autotext = value;
            textColorP.setVisible(!value);
            text = generateTextColor();
            textColorP.saveValue(text);
            callbackListener.setPrimaryTextColor(text);
            callbackListener.setSecondaryTextColor(getSecondaryTextColor());
            return true;
        });
        textColorP.setOnPreferenceChangeListener((preference, newValue) -> {
            text = (int) newValue;
            callbackListener.setPrimaryTextColor(text);
            callbackListener.setSecondaryTextColor(getSecondaryTextColor());
            return true;
        });

        //set defaults
        themeP.setValueIndex(theme);
        backgroundP.saveValue(background);
        transparencyP.setValue(transparency);
        textColorP.saveValue(text);
        textColorP.setVisible(!autotext);
        updateTheme();
    }

    private void updateTheme() {
        switch (theme) {
            case 0:
                background = ContextCompat.getColor(getContext(), R.color.widgetLightBackground);
                text = generateTextColor();
                transparency = 0;
                break;
            case 1:
                background = ContextCompat.getColor(getContext(), R.color.widgetDarkBackground);
                text = generateTextColor();
                transparency = 0;
                break;
        }
        if (theme < 2) {
            backgroundP.saveValue(background);
            transparencyP.setValue(transparency);
            autotextP.setChecked(true);
            textColorP.saveValue(text);

            backgroundP.setVisible(false);
            transparencyP.setVisible(false);
            autotextP.setVisible(false);
            textColorP.setVisible(false);

            callbackListener.setBackground(getBackgroundWithAlpha());
            callbackListener.setPrimaryTextColor(text);
            callbackListener.setSecondaryTextColor(getSecondaryTextColor());
        } else {
            backgroundP.setVisible(true);
            transparencyP.setVisible(true);
            autotextP.setVisible(true);
            textColorP.setVisible(!autotext);
        }
    }

    public void setCallbackListener(CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
        callbackListener.setBackground(getBackgroundWithAlpha());
        callbackListener.setPrimaryTextColor(text);
        callbackListener.setSecondaryTextColor(getSecondaryTextColor());
    }

    public void setWidgetID(int widgetID) {
        this.widgetID = widgetID;
        isWidgetIDSet = true;
    }

    private int getBackgroundWithAlpha() {
        return (background & 0x00ffffff) | ((255 - transparency) << 24);
    }

    private int getSecondaryTextColor() {
        return ColorUtils.blendARGB(background, text, 0.8f);
    }

    private int generateTextColor() {
        return Theme.Utils.textColorFor(background);
    }

    public void saveConfig() {
        if (isWidgetIDSet) {
            WidgetsSettings.Widget ws = new WidgetsSettings.Widget();

            ws.primaryTextSize = getResources().getDimension(R.dimen.widgetTextPrimary) / getResources().getDisplayMetrics().scaledDensity;
            ws.secondaryTextSize = getResources().getDimension(R.dimen.widgetTextSecondary) / getResources().getDisplayMetrics().scaledDensity;

            ws.primaryTextColor = text;
            ws.secondaryTextColor = getSecondaryTextColor();
            ws.backgroundColor = getBackgroundWithAlpha();

            AppSingleton appSingleton = AppSingleton.getInstance(getContext());

            appSingleton.getWidgetsSettings().widgetIds.add(widgetID);
            appSingleton.getWidgetsSettings().widgets.put(widgetID, ws);

            appSingleton.saveWidgetsSettings();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);
        outState.putInt("background", background);
        outState.putInt("transparency", transparency);
        outState.putInt("text", text);
        outState.putBoolean("autotext", autotext);
    }

    public static interface CallbackListener {
        public void setBackground(int color);

        public void setPrimaryTextColor(int color);

        public void setSecondaryTextColor(int color);
    }
}

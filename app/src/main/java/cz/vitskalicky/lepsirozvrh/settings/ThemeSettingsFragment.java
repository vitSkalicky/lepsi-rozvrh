package cz.vitskalicky.lepsirozvrh.settings;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.IOException;
import java.io.InputStream;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.theme.Theme;
import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;

public class ThemeSettingsFragment extends PreferenceFragmentCompat {

    public static Preference.OnPreferenceChangeListener valueToSummaryBinder = (preference, newValue) -> {
        String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        }/* else if (preference instanceof RingtonePreference) {
            // For ringtone preferences, look up the correct display value
            // using RingtoneManager.
            if (TextUtils.isEmpty(stringValue)) {
                // Empty values correspond to 'silent' (no ringtone).
                preference.setSummary(R.string.pref_ringtone_silent);

            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue));

                if (ringtone == null) {
                    // Clear the summary if there was a lookup error.
                    preference.setSummary(R.string.summary_choose_ringtone);
                } else {
                    // Set the summary to reflect the new ringtone display
                    // name.
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            }

        } else if (preference instanceof EditTextPreference) {
            if (preference.getKey().equals("key_gallery_name")) {
                // update the changed gallery name to summary filed
                preference.setSummary(stringValue);
            }
        }*/ else {
            preference.setSummary(stringValue);
        }
        return true;
    };
    private int detailLevel;
    private Preference exportPref = null;
    private Preference importPref = null;
    private Preference sharePref = null;
    private Utils.Listener recreateListener = null;
    private Utils.Listener exportListener = () -> {
    };
    private Utils.Listener importListener = () -> {
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        detailLevel = SharedPrefs.getIntPreference(getContext(), R.string.PREFS_DETAIL_LEVEL, 0);
        ListPreference themePref = findPreference(getString(R.string.PREFS_APP_THEME));
        themePref.setOnPreferenceChangeListener((preference, newValue) -> {
            int value = Integer.parseInt((String) newValue);
            if (value < 3) {
                int resId;
                switch (value) {
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
                    try (InputStream is = getResources().openRawResource(resId);) {
                        Theme.of(getContext()).setThemeData(Theme.loadThemeData(is));
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }
                });
                setDetailLevel(0);
            } else {
                if (detailLevel == 0) {
                    setDetailLevel(2);
                }
            }
            applyChanges();
            valueToSummaryBinder.onPreferenceChange(preference, newValue);
            return true;
        });
        int index = themePref.findIndexOfValue(themePref.getValue());
        // Set the summary to reflect the new value.
        themePref.setSummary(
                index >= 0
                        ? themePref.getEntries()[index]
                        : null);

        exportPref = findPreference(getString(R.string.PREFS_EXPORT_THEME));
        importPref = findPreference(getString(R.string.PREFS_IMPORT_THEME));
        sharePref = findPreference(getString(R.string.PREFS_SHARE_THEME));

        exportPref.setOnPreferenceClickListener(preference -> {
            Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage("Exporting theme.").build());
            exportListener.method();
            return true;
        });
        importPref.setOnPreferenceClickListener(preference -> {
            Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage("Importing theme.").build());
            importListener.method();
            return true;
        });
    }

    public void applyChanges() {
        if (recreateListener == null) {
            //this does no make a nice animation
            if (getActivity() != null) {
                getActivity().recreate();
            }
        } else {
            //if you do this correctly (see SettingsActivity) you get a nice animation
            recreateListener.method();
        }
    }

    private void setDetailLevel(int detailLevel) {
        this.detailLevel = detailLevel;
        SharedPrefs.setIntPreference(getContext(), R.string.PREFS_DETAIL_LEVEL, detailLevel);
    }

    public void setRecreateListener(Utils.Listener recreateListener) {
        this.recreateListener = recreateListener;
    }

    public void setExportListener(Utils.Listener exportListener) {
        this.exportListener = exportListener;
    }

    public void setImportListener(Utils.Listener importListener) {
        this.importListener = importListener;
    }
}

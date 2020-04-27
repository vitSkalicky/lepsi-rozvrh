package cz.vitskalicky.lepsirozvrh.settings;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;
import com.jaredrummler.cyanea.Cyanea;

import java.io.IOException;
import java.io.InputStream;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.theme.Theme;
import cz.vitskalicky.lepsirozvrh.theme.ThemeData;
import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;

public class ThemeSettingsFragment extends MyCyaneaPreferenceFragmentCompat {

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

        } else {
            preference.setSummary(stringValue);
        }
        return true;
    };
    private int detailLevel;
    private Cyanea cyanea;
    private Theme theme;

    private Preference exportPref;
    private Preference importPref;

    private PreferenceCategory customBasics;
    private ColorPreferenceCompat primaryPref;
    private ColorPreferenceCompat accentPref;
    private ColorPreferenceCompat backgroundPref;
    private PreferenceCategory cellsFill;
    private PreferenceCategory other;
    private Preference more;
    private Preference less;

    private PreferenceCategory ctgrHeader;
    private PreferenceCategory ctgrNormal;
    private PreferenceCategory ctgrChange;
    private PreferenceCategory ctgrNoSchool;
    private PreferenceCategory ctgrEmpty;

    private ColorPreferenceCompat prefcEmptyBg;
    private ColorPreferenceCompat prefcABg;
    private ColorPreferenceCompat prefcHBg;
    private ColorPreferenceCompat prefcChngBg;
    private ColorPreferenceCompat prefcHeaderBg;
    private ColorPreferenceCompat prefcInfolineText;
    private EditTextPreference dpDividerWidth;
    private EditTextPreference dpHighlightWidth;
    private EditTextPreference spInfolineTextSize;

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
        cyanea = Cyanea.getInstance();
        theme = Theme.of(getContext());
        SharedPrefs.setInt(getContext(), "cpc-color-primary", cyanea.getPrimary());
        SharedPrefs.setInt(getContext(), "cpc-color-accent", cyanea.getAccent());
        SharedPrefs.setInt(getContext(), "cpc-color-background", cyanea.getBackgroundColor());
        super.onCreate(savedInstanceState);
        SharedPrefs.setBooleanPreference(getContext(), R.string.THEME_CHANGED, true);

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
                    ThemeData td = null;
                    Exception e = null;
                    try (InputStream is = getResources().openRawResource(resId);) {
                        td = ThemeData.parseJson(is);
                    } catch (IOException | NullPointerException ex) {
                        e = ex;
                        e.printStackTrace();
                    }
                    final ThemeData ftd = td;
                    final Exception fe = e;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (ftd != null) {
                            Theme.of(getContext()).setThemeData(ftd);
                            applyChanges();
                        } else if (getView() != null) {
                            Utils.somethingWrong(fe, getView(), getContext());
                        }
                    });
                });
                setDetailLevel(0);
            } else {
                if (detailLevel == 0) {
                    setDetailLevel(2);
                }
            }
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

        primaryPref = findPreference("cpc-color-primary");
        accentPref = findPreference("cpc-color-accent");
        backgroundPref = findPreference("cpc-color-background");

        customBasics = findPreference("ctgr-custom-basic");
        cellsFill = findPreference("ctgr-cells-fill");
        ctgrNormal = findPreference("ctgr-normal-lesson");
        ctgrChange = findPreference("ctgr-change");
        ctgrNoSchool = findPreference("ctgr-no-school");
        ctgrEmpty = findPreference("ctgr-empty");
        ctgrHeader = findPreference("ctgr-header");
        other = findPreference("ctgr-other");

        prefcEmptyBg = findPreference(getString(R.string.PREFS_THEME_cEmptyBg));
        prefcABg = findPreference(getString(R.string.PREFS_THEME_cABg));
        prefcHBg = findPreference(getString(R.string.PREFS_THEME_cHBg));
        prefcChngBg = findPreference(getString(R.string.PREFS_THEME_cChngBg));
        prefcHeaderBg = findPreference(getString(R.string.PREFS_THEME_cHeaderBg));

        prefcInfolineText = findPreference(getString(R.string.PREFS_THEME_cInfolineText));

        dpDividerWidth = findPreference("etp-dpDividerWidth");
        dpHighlightWidth = findPreference("etp-dpHighlightWidth");
        spInfolineTextSize = findPreference("etp-spInfolineTextSize");

        more = findPreference("p-more");
        less = findPreference("p-less");

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

        primaryPref.setOnPreferenceChangeListener((preference, newValue) -> {
            cyanea.edit().primary((Integer) newValue).apply();
            theme.regenerateColors(detailLevel);
            dismissDialog(primaryPref); //this prevents a bug where the dialog appears again after activity recreation
            applyChanges();
            return true;
        });
        accentPref.setOnPreferenceChangeListener((preference, newValue) -> {
            cyanea.edit().accent((Integer) newValue).apply();
            theme.regenerateColors(detailLevel);
            dismissDialog(accentPref); //this prevents a bug where the dialog appears again after activity recreation
            applyChanges();
            return true;
        });
        backgroundPref.setOnPreferenceChangeListener((preference, newValue) -> {
            cyanea.edit().background((Integer) newValue).apply();
            theme.regenerateColors(detailLevel);
            dismissDialog(backgroundPref); //this prevents a bug where the dialog appears again after activity recreation
            applyChanges();
            return true;
        });

        dpDividerWidth.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        dpDividerWidth.setText(Float.toString(theme.getDpDividerWidth()));
        dpDividerWidth.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                Float parsed = Float.parseFloat(newValue.toString().replace(',', '.'));
                theme.setDpDividerWidth(parsed);
                return true;
            } catch (NumberFormatException e) {
                Snackbar.make(getView(), R.string.not_a_float_error, BaseTransientBottomBar.LENGTH_LONG).show();
                return false;
            }
        });
        dpHighlightWidth.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        dpHighlightWidth.setText(Float.toString(theme.getDpHighlightWidth()));
        dpHighlightWidth.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                Float parsed = Float.parseFloat(newValue.toString().replace(',', '.'));
                theme.setDpHighlightWidth(parsed);
                return true;
            } catch (NumberFormatException e) {
                Snackbar.make(getView(), R.string.not_a_float_error, BaseTransientBottomBar.LENGTH_LONG).show();
                return false;
            }
        });
        spInfolineTextSize.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        spInfolineTextSize.setText(Float.toString(theme.getSpInfolineTextSize()));
        spInfolineTextSize.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                Float parsed = Float.parseFloat(newValue.toString().replace(',', '.'));
                theme.setSpInfolineTextSize(parsed);
                return true;
            } catch (NumberFormatException e) {
                Snackbar.make(getView(), R.string.not_a_float_error, BaseTransientBottomBar.LENGTH_LONG).show();
                return false;
            }
        });

        more.setOnPreferenceClickListener(preference -> {
            setDetailLevel(detailLevel + 1);
            return true;
        });
        less.setOnPreferenceClickListener(preference -> {
            setDetailLevel(detailLevel - 1);
            return true;
        });

        setDetailLevel(detailLevel);
    }

    private void dismissDialog(ColorPreferenceCompat pref) {
        if (getActivity() != null) {
            Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(pref.getFragmentTag());
            if (f instanceof DialogFragment) {
                ((DialogFragment) f).dismiss();
            }
        }
    }

    public void applyChanges() {
        if (getActivity() instanceof Utils.RecreateWithAnimationActivity) {
            ((Utils.RecreateWithAnimationActivity) getActivity()).recreateWithAnimation();
        } else {
            getActivity().recreate();
        }
    }

    private void setDetailLevel(int detailLevel) {
        int oldDetaillevel = this.detailLevel;
        this.detailLevel = detailLevel;
        SharedPrefs.setIntPreference(getContext(), R.string.PREFS_DETAIL_LEVEL, detailLevel);

        if (detailLevel < 1) {
            exportPref.setVisible(false);
            importPref.setVisible(false);
            customBasics.setVisible(false);
        } else {
            exportPref.setVisible(true);
            importPref.setVisible(true);
            customBasics.setVisible(true);
        }
        if (detailLevel < 2) {
            cellsFill.setVisible(false);
            other.setVisible(false);
        } else {
            cellsFill.setVisible(true);
            other.setVisible(true);
        }
        if (detailLevel < 3) {
            //cellsFill.setVisible(true);
            ctgrNormal.setVisible(false);
            ctgrChange.setVisible(false);
            ctgrNoSchool.setVisible(false);
            ctgrEmpty.setVisible(false);
            ctgrHeader.setVisible(false);

            ctgrEmpty.removePreference(prefcEmptyBg);
            ctgrNoSchool.removePreference(prefcABg);
            ctgrNormal.removePreference(prefcHBg);
            ctgrChange.removePreference(prefcChngBg);
            ctgrHeader.removePreference(prefcHeaderBg);
            cellsFill.addPreference(prefcEmptyBg);
            cellsFill.addPreference(prefcABg);
            cellsFill.addPreference(prefcHBg);
            cellsFill.addPreference(prefcChngBg);
            cellsFill.addPreference(prefcHeaderBg);
            prefcEmptyBg.setTitle(R.string.type_empty);
            prefcABg.setTitle(R.string.type_no_school);
            prefcHBg.setTitle(R.string.type_normal_lesson);
            prefcChngBg.setTitle(R.string.type_change);
            prefcHeaderBg.setTitle(R.string.type_header);
            prefcEmptyBg.setOrder(4);
            prefcABg.setOrder(3);
            prefcHBg.setOrder(1);
            prefcChngBg.setOrder(2);
            prefcHeaderBg.setOrder(5);
            prefcABg.setSummary(R.string.type_no_school_desc);
            prefcChngBg.setSummary(R.string.type_change_desc);
            prefcHeaderBg.setSummary(R.string.type_header_desc);

            ctgrNormal.removePreference(prefcHBg);
            cellsFill.addPreference(prefcHBg);
            prefcHBg.setTitle(R.string.type_normal_lesson);

            prefcInfolineText.setVisible(false);
            spInfolineTextSize.setVisible(false);

        } else {
            cellsFill.setVisible(false);
            ctgrNormal.setVisible(true);
            ctgrChange.setVisible(true);
            ctgrNoSchool.setVisible(true);
            ctgrEmpty.setVisible(true);
            ctgrHeader.setVisible(true);

            cellsFill.removePreference(prefcEmptyBg);
            cellsFill.removePreference(prefcABg);
            cellsFill.removePreference(prefcHBg);
            cellsFill.removePreference(prefcChngBg);
            cellsFill.removePreference(prefcHeaderBg);
            ctgrEmpty.addPreference(prefcEmptyBg);
            ctgrNoSchool.addPreference(prefcABg);
            ctgrNormal.addPreference(prefcHBg);
            ctgrChange.addPreference(prefcChngBg);
            ctgrHeader.addPreference(prefcHeaderBg);
            prefcEmptyBg.setTitle(R.string.cell_background);
            prefcABg.setTitle(R.string.cell_background);
            prefcHBg.setTitle(R.string.cell_background);
            prefcChngBg.setTitle(R.string.cell_background);
            prefcHeaderBg.setTitle(R.string.cell_background);
            prefcEmptyBg.setOrder(1);
            prefcABg.setOrder(1);
            prefcHBg.setOrder(1);
            prefcChngBg.setOrder(1);
            prefcHeaderBg.setOrder(1);
            prefcABg.setSummary(null);
            prefcChngBg.setSummary(null);
            prefcHeaderBg.setSummary(null);

            prefcInfolineText.setVisible(true);
            spInfolineTextSize.setVisible(true);
        }
        if (detailLevel > 0 && detailLevel < 3) {
            more.setVisible(true);
        } else {
            more.setVisible(false);
        }
        if (detailLevel > 1) {
            less.setVisible(true);
        } else {
            less.setVisible(false);
        }
        if (oldDetaillevel > detailLevel) {
            theme.regenerateColors(detailLevel);
        }
    }

    public void setExportListener(Utils.Listener exportListener) {
        this.exportListener = exportListener;
    }

    public void setImportListener(Utils.Listener importListener) {
        this.importListener = importListener;
    }
}

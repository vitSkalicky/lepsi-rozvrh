package cz.vitskalicky.lepsirozvrh.settings;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private LogoutListener logoutListener = () -> {};

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findPreference(getString(R.string.PREFS_LOGOUT)).setOnPreferenceClickListener(preference -> {
            logoutListener.onLogout();
            return true;
        });

        Preference userInfo = findPreference(getString(R.string.PREFS_USER));
        userInfo.setTitle(SharedPrefs.getString(getContext(), SharedPrefs.NAME));
        String type = SharedPrefs.getString(getContext(), SharedPrefs.TYPE);
        switch (type){
            case "Z":
                userInfo.setSummary(R.string.student);
                break;
            case "R":
                userInfo.setSummary(R.string.parent);
                break;
            case "U":
                userInfo.setSummary(R.string.teacher);
                break;
        }

        ListPreference switchToNextWeek = findPreference(getString(R.string.PREFS_SWITCH_TO_NEXT_WEEK));
        switchToNextWeek.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());

    }

    public void setLogoutListener(LogoutListener listener){
        this.logoutListener = listener;
    }

    public static interface LogoutListener {
        public void onLogout();
    }
}

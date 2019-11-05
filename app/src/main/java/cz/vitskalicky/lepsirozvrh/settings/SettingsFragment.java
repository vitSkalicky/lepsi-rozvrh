package cz.vitskalicky.lepsirozvrh.settings;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private LogoutListener logoutListener = () -> {
    };

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

        findPreference(getString(R.string.PREFS_SEND_FEEDBACK)).setOnPreferenceClickListener(preference -> {
            AlertDialog ad = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.include_schedule)
                    .setMessage(R.string.include_schedule_desc)
                    .setNegativeButton(R.string.no, (dialog, which) -> sendFeedback(false))
                    .setPositiveButton(R.string.yes, (dialog, which) -> sendFeedback(true))
                    .setOnCancelListener(dialog -> sendFeedback(false))
                    .create();
            ad.show();
            return true;
        });

        Preference userInfo = findPreference(getString(R.string.PREFS_USER));
        userInfo.setTitle(SharedPrefs.getString(getContext(), SharedPrefs.NAME));
        String type = SharedPrefs.getString(getContext(), SharedPrefs.TYPE);
        switch (type) {
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

    public void setLogoutListener(LogoutListener listener) {
        this.logoutListener = listener;
    }

    public static interface LogoutListener {
        public void onLogout();
    }

    public void sendFeedback(boolean includeRozvrh) {
        String body = null;
        try {
            body = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\n" + getContext().getString(R.string.email_message) + "\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
            if (includeRozvrh) {
                try {
                    RozvrhAPI rozvrhAPI = AppSingleton.getInstance(getContext()).getRozvrhAPI();
                    Rozvrh current = rozvrhAPI.get(Utils.getDisplayWeekMonday(getContext()), (code, rozvrh1) -> {}, (code, rozvrh1) -> {});
                    Rozvrh permanent = rozvrhAPI.get(null, (code, rozvrh1) -> {}, (code, rozvrh1) -> {});

                    if (current == null) {
                        body += "\nCurrent schedule is null";
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Serializer serializer = new Persister();
                        serializer.write(current, baos);

                        body += "\nCurrent schedule:\n\n" + baos.toString() + "\n";
                    }

                    if (permanent == null) {
                        body += "\nPermanent schedule is null";
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Serializer serializer = new Persister();
                        serializer.write(permanent, baos);

                        body += "\nPermanent schedule:\n\n" + baos.toString() + "\n";
                    }
                } catch (Exception e) {
                }
            }
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
        }


        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        String address = getContext().getString(R.string.CONTACT_MAIL);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            getContext().startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Snackbar snackbar = Snackbar.make(getView(), getText(R.string.no_email_client), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.copy_address, v -> {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(address, address);
                clipboard.setPrimaryClip(clip);
                Snackbar.make(getView(), R.string.copied, Snackbar.LENGTH_SHORT).show();
            });
            snackbar.show();
        }
    }
}

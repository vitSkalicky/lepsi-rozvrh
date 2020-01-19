package cz.vitskalicky.lepsirozvrh.settings;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import cz.vitskalicky.lepsirozvrh.BuildConfig;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;
import io.sentry.Sentry;

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

        SwitchPreferenceCompat sendCrashReportsPreference = findPreference(getString(R.string.PREFS_SEND_CRASH_REPORTS));
        //Crash reports are allowed on official release builds only. (see build.gradle)
        if (BuildConfig.ALLOW_SENTRY){
            sendCrashReportsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue instanceof Boolean && getActivity() != null) {
                    boolean value = (boolean) newValue;
                    if (value) {
                        ((MainApplication) getActivity().getApplication()).enableSentry();
                    } else {
                        ((MainApplication) getActivity().getApplication()).diableSentry();
                    }
                }
                return true;
            });
            sendCrashReportsPreference.setVisible(true);
        }else {
            sendCrashReportsPreference.setVisible(false);
        }

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
        findPreference(getString(R.string.PREFS_OSS_LICENCES)).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), OssLicensesMenuActivity.class);
            startActivity(intent);
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

        SwitchPreferenceCompat notificationPreference = findPreference(getString(R.string.PREFS_NOTIFICATION));
        notificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                PermanentNotification.showInfoDialog(getContext(), false);
                ((MainApplication) getContext().getApplicationContext()).enableNotification();
            } else {
                ((MainApplication) getContext().getApplicationContext()).disableNotification();
            }
            return true;
        });

        Preference appVersionPreference = findPreference(getString(R.string.PREFS_APP_VERSION));
        String versionText = BuildConfig.FLAVOR + "-" + BuildConfig.BUILD_TYPE + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.GitHash + ")";
        appVersionPreference.setSummary(versionText);
        appVersionPreference.setOnPreferenceClickListener(preference -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(versionText, versionText);
            clipboard.setPrimaryClip(clip);
            Snackbar.make(getView(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
            return true;
        });
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
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Commit hash: " + BuildConfig.GitHash + "Build type: " + BuildConfig.BUILD_TYPE + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
            if (Sentry.getContext() != null && Sentry.getContext().getUser() != null){
                body += "\n Sentry client id: " + Sentry.getStoredClient().getContext().getUser().getId();
            }else {
                body += "\n Sentry client id not available";
            }
            body += "\n Sentry enabled: " + SharedPrefs.getBooleanPreference(getContext(), R.string.PREFS_SEND_CRASH_REPORTS);
            final String finBody = body;
            if (includeRozvrh) {
                new Thread(() -> {
                    String fileCurrent = "rozvrh-" + Utils.dateToString(Utils.getDisplayWeekMonday(getContext())) + ".xml";
                    String filePerm = "rozvrh-perm.xml";

                    String current = "";
                    String permanent = "";
                    try (FileInputStream inputStream = getContext().openFileInput(fileCurrent)) {
                        //converts inputStream to string
                        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                        current = s.hasNext() ? s.next() : "";
                    } catch (FileNotFoundException e) {
                        current = "File not found: " + e.getMessage();
                    } catch (IOException e) {
                        current = "IOException: " + e.getMessage();
                    }
                    try (FileInputStream inputStream = getContext().openFileInput(filePerm)) {
                        //converts inputStream to string
                        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                        permanent = s.hasNext() ? s.next() : "";
                    } catch (FileNotFoundException e) {
                        permanent = "File not found: " + e.getMessage();
                    } catch (IOException e) {
                        permanent = "IOException: " + e.getMessage();
                    }

                    String finCurrent = current;
                    String finPermanent = permanent;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        String newBody = finBody;
                        newBody += "\nCurrent schedule:\n\n" + finCurrent + "\n";
                        newBody += "\nPermanent schedule:\n\n" + finPermanent + "\n";

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
                        String address = getContext().getString(R.string.CONTACT_MAIL);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        intent.putExtra(Intent.EXTRA_TEXT, newBody);

                        try {
                            getContext().startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Snackbar snackbar = Snackbar.make(getView(), getText(R.string.no_email_client), Snackbar.LENGTH_LONG);
                            snackbar.setAction(R.string.copy_address, v -> {
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText(address, address);
                                clipboard.setPrimaryClip(clip);
                                Snackbar.make(getView(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
                            });
                            snackbar.show();
                        }
                    });

                }).run();
            } else {
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
                        Snackbar.make(getView(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
                    });
                    snackbar.show();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getContext(),"!",Toast.LENGTH_SHORT).show();
        }
    }
}

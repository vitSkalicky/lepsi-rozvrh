/*
 Initially taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2020
*/
package cz.vitskalicky.lepsirozvrh;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import io.sentry.Sentry;

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    public static String parseDate(String rawDate, String inputFormat, String outputFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(inputFormat, Locale.US);
        SimpleDateFormat readable = new SimpleDateFormat(outputFormat, Locale.US);

        try {
            Date date = sdf.parse(rawDate);
            return readable.format(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static int minutesOfDay(String t) {
        String time[] = t.split(":");
        int hours = Integer.valueOf(time[0]);
        int minutes = Integer.valueOf(time[1]);
        return minutes + hours * 60;
    }

    public static LocalDate getWeekMonday(LocalDate date) {
        if (date == null) return null;
        return date.dayOfWeek().setCopy(DateTimeConstants.MONDAY);

    }

    public static String dateToString(LocalDate date) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
        return dtf.print(date);
    }

    public static LocalDate parseDate(String date) {
        if (date == null || date.equals("")) return null;
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
        return dtf.parseLocalDate(date);
    }

    public static LocalDate getCurrentMonday() {
        return getWeekMonday(LocalDate.now());
    }

    public static LocalDate getDisplayWeekMonday(Context context) {
        int offset = 2;
        if (SharedPrefs.containsPreference(context, R.string.PREFS_SWITCH_TO_NEXT_WEEK)) {
            try {
                offset = Integer.parseInt(SharedPrefs.getString(context, context.getString(R.string.PREFS_SWITCH_TO_NEXT_WEEK)));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to cast 'Switch to the next week' setting value. Value: " + SharedPrefs.getString(context, context.getString(R.string.PREFS_SWITCH_TO_NEXT_WEEK)));
            }
        }

        return getWeekMonday(LocalDate.now().plusDays(offset));
    }

    /**
     * For debugging purposes
     */
    public static String getDebugTime() {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("mm:ss.SSS");
        LocalTime time = LocalTime.now();
        return dtf.print(time);
    }

    /**
     * Get fucking localized string for week info
     *
     * @param week week relative to now: 0 - current, 1 - next, -1 previous, {@code Integer.MAX_VALUE} permanent schedule
     * @return Localize, human friendly string
     */
    @SuppressWarnings("ConstantConditions")
    public static String getfl10nedWeekString(int week, Context context) {
        switch (week) {
            case 0:
                return context.getString(R.string.info_this_week);
            case 1:
                return context.getString(R.string.info_next_week);
            case -1:
                return context.getString(R.string.info_last_week);
            case Integer.MAX_VALUE:
                return context.getString(R.string.info_permanent);
        }
        if (week > 0)
            return context.getResources().getQuantityString(R.plurals.info_weeks_forward, week, week);
        else
            return context.getResources().getQuantityString(R.plurals.info_weeks_back, -1 * week, -1 * week);
    }

    public static interface Listener{
        public void method();
    }

    /**
     * Asks the user to send his Rozvrh because it is weird.

     * @param forToast View to use for displaying snackbar
     * @param rozvrhDate rozvrh date, null for permanent
     */
    public static void wtfRozvrh(Context context, View forToast, LocalDate rozvrhDate){

        //similar to {@link cz.vitskalicky.lepsirozvrh.settings.SettingsFragment#sendFeedback(boolean, Context, View)}

        LocalDate silent;
        try {
            silent = Utils.parseDate(SharedPrefs.getString(context, SharedPrefs.DISABLE_WTF_ROZVRH_UP_TO_DATE));
        }catch (IllegalArgumentException e){
            silent = null;
        }

        if (silent != null && LocalDate.now().isBefore(silent)){
            return;
        }

        AlertDialog ad = new AlertDialog.Builder(context)
                .setTitle(R.string.wtf_rozvrh_title)
                .setMessage(R.string.wtf_rozvrh_message)
                .setPositiveButton(R.string.wtf_rozvrh_report, (dialog, which) -> {
                    String body = null;
                    try {
                        body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                        body = "\n\n-----------------------------\n" + context.getString(R.string.email_message) + "\n Device OS: Android \n Device OS version: " +
                                Build.VERSION.RELEASE + "\n App Version: " + body + "\n Commit hash: " + BuildConfig.GitHash + "Build type: " + BuildConfig.BUILD_TYPE + "\n Device Brand: " + Build.BRAND +
                                "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
                        if (Sentry.getContext() != null && Sentry.getContext().getUser() != null){
                            body += "\n Sentry client id: " + Sentry.getStoredClient().getContext().getUser().getId();
                        }else {
                            body += "\n Sentry client id not available";
                        }
                        body += "\n Sentry enabled: " + SharedPrefs.getBooleanPreference(context, R.string.PREFS_SEND_CRASH_REPORTS);
                        final String finBody = body;
                        new Thread(() -> {
                            String fileName;
                            if (rozvrhDate != null) {
                                fileName = "rozvrh-" + Utils.dateToString(Utils.getWeekMonday(rozvrhDate)) + ".xml";
                            } else {
                                fileName = "rozvrh-perm.xml";
                            }

                            String rozvrh = "";
                            try (FileInputStream inputStream = context.openFileInput(fileName)) {
                                //converts inputStream to string
                                Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                                rozvrh = s.hasNext() ? s.next() : "";
                            } catch (FileNotFoundException e) {
                                rozvrh = "File not found: " + e.getMessage();
                            } catch (IOException e) {
                                rozvrh = "IOException: " + e.getMessage();
                            }

                            String finRozvrh = rozvrh;

                            new Handler(Looper.getMainLooper()).post(() -> {
                                String newBody = finBody + "\nSchedule:\n\n" + finRozvrh + "\n";

                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("message/rfc822");
                                String address = context.getString(R.string.CONTACT_MAIL);
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                                intent.putExtra(Intent.EXTRA_TEXT, newBody);

                                try {
                                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)));
                                } catch (ActivityNotFoundException ex) {
                                    Snackbar snackbar = Snackbar.make(forToast, context.getText(R.string.no_email_client), Snackbar.LENGTH_LONG);
                                    snackbar.setAction(R.string.copy_address, v -> {
                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText(address + "\n\n" + newBody, address + "\n\n" + newBody);
                                        clipboard.setPrimaryClip(clip);
                                        Snackbar.make(forToast, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
                                    });
                                    snackbar.show();
                                }
                            });

                        }).start();
                    } catch (PackageManager.NameNotFoundException e) {
                        Toast.makeText(context,"!",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.wtf_rozvrh_later, (dialog, which) -> {})
                .setNeutralButton(R.string.wtf_not_fot_month, (dialog, which) -> {
                    SharedPrefs.setString(context, SharedPrefs.DISABLE_WTF_ROZVRH_UP_TO_DATE, Utils.dateToString(LocalDate.now().plusDays(30)));
                })
                .create();
        ad.show();
    }

    public static void somethingWrong(Exception e, View forToast, Context context){
        Snackbar.make(forToast, R.string.something_went_wron, BaseTransientBottomBar.LENGTH_LONG)
                .setAction(R.string.report,v -> {
                    sendFeedback(false, e, context, forToast);
                })
                .show();
    }

    public static void sendFeedback(boolean includeRozvrh,@Nullable Exception exept, Context context, @Nullable View forToast) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\n" + context.getString(R.string.email_message) + "\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Commit hash: " + BuildConfig.GitHash + "Build type: " + BuildConfig.BUILD_TYPE + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
            if (Sentry.getContext() != null && Sentry.getContext().getUser() != null){
                body += "\n Sentry client id: " + Sentry.getStoredClient().getContext().getUser().getId();
            }else {
                body += "\n Sentry client id not available";
            }
            body += "\n Sentry enabled: " + SharedPrefs.getBooleanPreference(context, R.string.PREFS_SEND_CRASH_REPORTS);
            final String finBody = body;
            if (exept != null){
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exept.printStackTrace(pw);
                body += "\nException stack trace:\n\n" + sw.toString();
            }
            if (includeRozvrh) {
                new Thread(() -> {
                    String fileCurrent = "rozvrh-" + Utils.dateToString(Utils.getDisplayWeekMonday(context)) + ".xml";
                    String filePerm = "rozvrh-perm.xml";

                    String current = "";
                    String permanent = "";
                    try (FileInputStream inputStream = context.openFileInput(fileCurrent)) {
                        //converts inputStream to string
                        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                        current = s.hasNext() ? s.next() : "";
                    } catch (FileNotFoundException e) {
                        current = "File not found: " + e.getMessage();
                    } catch (IOException e) {
                        current = "IOException: " + e.getMessage();
                    }
                    try (FileInputStream inputStream = context.openFileInput(filePerm)) {
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
                        String address = context.getString(R.string.CONTACT_MAIL);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        intent.putExtra(Intent.EXTRA_TEXT, newBody);

                        try {
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Snackbar snackbar = Snackbar.make(forToast, context.getText(R.string.no_email_client), Snackbar.LENGTH_LONG);
                            snackbar.setAction(R.string.copy_address, v -> {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText(address, address);
                                clipboard.setPrimaryClip(clip);
                                Snackbar.make(forToast, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
                            });
                            snackbar.show();
                        }
                    });

                }).run();
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                String address = context.getString(R.string.CONTACT_MAIL);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, body);

                try {
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Snackbar snackbar = Snackbar.make(forToast, context.getText(R.string.no_email_client), Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.copy_address, v -> {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(address, address);
                        clipboard.setPrimaryClip(clip);
                        Snackbar.make(forToast, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
                    });
                    snackbar.show();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context,"!",Toast.LENGTH_SHORT).show();
        }
    }

    public static interface RecreateWithAnimationActivity{
        /**
         * Use this to recreate an activity with some nice animation instead of just "blink", which is what {@link Activity#recreate()} does.
         */
        public void recreateWithAnimation();
    }
}

/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh;

import android.content.Context;
import android.util.Log;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
}

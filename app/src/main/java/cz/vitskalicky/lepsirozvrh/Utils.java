/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {
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
        return minutes+hours*60;
    }

    public static Calendar getWeekMonday(Calendar date){
        Calendar ret = Calendar.getInstance();
        ret.clear();
        ret.set(Calendar.DATE, date.get(Calendar.DATE));
        ret.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return ret;
    }

    public static String dateToString(Calendar date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }

    public static Calendar getCurrentMonday(){
        return getWeekMonday(Calendar.getInstance());
    }
}

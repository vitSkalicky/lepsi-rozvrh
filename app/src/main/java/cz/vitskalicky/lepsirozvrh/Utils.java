/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public static LocalDate getWeekMonday(LocalDate date){
        return date.dayOfWeek().setCopy(DateTimeConstants.MONDAY);

    }

    public static String dateToString(LocalDate date){
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
        return dtf.print(date);
    }

    public static LocalDate parseDate(String date){
        if (date == null || date.equals("")) return null;
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
        return dtf.parseLocalDate(date);
    }

    public static String dateToLoacalizedString(LocalDate date){
        if (date == null) return "";
        DateTimeFormatter dtf = DateTimeFormat.shortDate();
        return dtf.print(date);
    }

    public static LocalDate getCurrentMonday(){
        return getWeekMonday(LocalDate.now());
    }
}

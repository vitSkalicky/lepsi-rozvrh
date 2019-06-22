package cz.vitskalicky.lepsirozvrh.item;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RozvrhDay {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private String shortcut;
    private Calendar date;

    /**
     * Lesson for each caption
     *
     * lessons[0] - list of lessons in 0th caption in {@link Rozvrh#lessonCaptions}
     */
    private RozvrhLesson[][] lessons;

    //<editor-fold desc="Getters">
    public String getShortcut() {
        return shortcut;
    }

    public Calendar getDate() {
        return date;
    }

    public String getLocalizedDate() {
        DateFormat localDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
        return localDateFormat.format(getDate());
    }

    public RozvrhLesson[][] getLessons() {
        return lessons;
    }
    //</editor-fold>


    public RozvrhDay(String shortcut, Calendar date, RozvrhLesson[][] lessons) {
        this.shortcut = shortcut;
        this.date = date;
        this.lessons = lessons;
    }
}

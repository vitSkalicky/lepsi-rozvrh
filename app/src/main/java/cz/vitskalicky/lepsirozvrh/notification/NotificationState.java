package cz.vitskalicky.lepsirozvrh.notification;

import android.content.Context;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;


import cz.vitskalicky.lepsirozvrh.SharedPrefs;

public class NotificationState {
    private static final String TAG = NotificationState.class.getSimpleName();
    public static final String SHARED_PREF_OFFSET = "notification-offset";
    public static final String SHARED_PREF_OFFSET_TIME = "notification-offset-time";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private Context context;

    public LocalDateTime scheduledNotificationTime = null;

    private int offset = 0;
    private LocalDateTime offsetTime = null;

    private static final int RESET_OFFSET_AFTER_MINUTES = 5;

    public NotificationState(Context context) {
        this.context = context;
        offset = SharedPrefs.getInt(context, SHARED_PREF_OFFSET);
        String offsetString = SharedPrefs.getString(context, SHARED_PREF_OFFSET_TIME);
        if (!offsetString.isEmpty()){
            offsetTime = LocalDateTime.parse(offsetString, dateTimeFormatter);
        }else {
            offsetTime = null;
        }
    }

    public int getOffset() {
        if (offsetTime == null || offsetTime.isBefore(LocalDateTime.now().minusMinutes(RESET_OFFSET_AFTER_MINUTES))){
            offset = 0;
            offsetTime = null;
        }
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        SharedPrefs.setInt(context, SHARED_PREF_OFFSET, offset);
        if (offset != 0) {
            offsetTime = LocalDateTime.now();
            SharedPrefs.setString(context, SHARED_PREF_OFFSET_TIME, offsetTime.toString(dateTimeFormatter));
        }else {
            offsetTime = null;
            SharedPrefs.setString(context, SHARED_PREF_OFFSET_TIME, "");
        }
    }

    public LocalDateTime getOffsetTime() {
        if (offsetTime == null || offsetTime.isBefore(LocalDateTime.now().minusMinutes(RESET_OFFSET_AFTER_MINUTES))){
            offset = 0;
            offsetTime = null;
        }
        return offsetTime;
    }

    public boolean isOffset(){
        return getOffset() != 0;
    }

    /**
     * null - no offset set
     */
    public LocalDateTime getOffsetResetTime(){
        if (getOffsetTime() == null){
            return null;
        }
        return getOffsetTime().plusMinutes(RESET_OFFSET_AFTER_MINUTES);
    }

}

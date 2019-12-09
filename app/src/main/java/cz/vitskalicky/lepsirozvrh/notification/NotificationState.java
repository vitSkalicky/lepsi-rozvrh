package cz.vitskalicky.lepsirozvrh.notification;

import org.joda.time.LocalDateTime;

public class NotificationState {
    public LocalDateTime scheduledNotificationTime = null;

    private int offset = 0;
    private LocalDateTime offsetTime = null;

    private static final int RESET_OFFSET_AFTER_MINUTES = 5;

    public int getOffset() {
        if (offsetTime == null || offsetTime.isBefore(LocalDateTime.now().minusMinutes(RESET_OFFSET_AFTER_MINUTES))){
            offset = 0;
            offsetTime = null;
        }
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        if (offset != 0) {
            offsetTime = LocalDateTime.now();
        }else {
            offsetTime = null;
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

package cz.vitskalicky.lepsirozvrh;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.joda.time.LocalDateTime;

import java.util.Random;

import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhWrapper;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.notification.NotiBroadcastReciever;
import cz.vitskalicky.lepsirozvrh.notification.NotificationState;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;
import cz.vitskalicky.lepsirozvrh.widget.AppWidgetProvider;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.User;


public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();

    private NotificationState notificationState = null;
    private LiveData<RozvrhWrapper> notificationLiveData = null;
    private Observer<RozvrhWrapper> notificationObserver = rozvrhWrapper -> {
        if (!SharedPrefs.getBooleanPreference(this, R.string.PREFS_NOTIFICATION, true)) {
            return;
        }
        PermanentNotification.update(rozvrhWrapper.getRozvrh(), this);
    };
    private LocalDateTime widgetUpdateTime = null;
    private Observer<RozvrhWrapper> widgetObserver = rozvrhWrapper -> {
        if (rozvrhWrapper.getRozvrh() != null) {
            AppWidgetProvider.update(rozvrhWrapper.getRozvrh(), this);
            checkWidgetUpdate(rozvrhWrapper.getRozvrh());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Sentry (crash report) client
        if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_SEND_CRASH_REPORTS)) {
            enableSentry();
        } else {
            diableSentry();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Register notification channel for the permanent notification
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_detials);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(PermanentNotification.PERMANENT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.silence), new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
            channel.setShowBadge(false);
            channel.setVibrationPattern(null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        notificationState = new NotificationState(this);
        if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_NOTIFICATION, true)) {
            enableNotification();
        } else {
            disableNotification();
        }

        RozvrhAPI rozvrhAPI = AppSingleton.getInstance(this).getRozvrhAPI();
        rozvrhAPI.getCurrentWeekLiveData().observeForever(widgetObserver);
    }

    public LocalDateTime getScheduledNotificationTime() {
        return notificationState.scheduledNotificationTime;
    }

    public NotificationState getNotificationState() {
        return notificationState;
    }

    /**
     * Schedules AlarmManager for notification update
     *
     * @param triggerTime
     */
    public void scheduleNotificationUpdate(LocalDateTime triggerTime) {
        if (triggerTime == null) {
            triggerTime = LocalDateTime.now().plusDays(1);
        }
        if (notificationState.getOffsetResetTime() != null && triggerTime.isAfter(notificationState.getOffsetResetTime())) {
            triggerTime = notificationState.getOffsetResetTime();
        }
        PendingIntent pendingIntent = getNotiPendingIntent(this);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime.toDate().getTime(), 60 * 60000, pendingIntent);

        Log.d(TAG, "Scheduled a notificatio upadate on " + triggerTime.toString("MM-dd HH:mm:ss"));
        notificationState.scheduledNotificationTime = triggerTime;
    }

    public void checkWidgetUpdate(Rozvrh rozvrh) {
        if (rozvrh == null){
            return;
        }
        Rozvrh.GetNCLCTreturnValues values = rozvrh.getNextCurrentLessonChangeTime();
        if (widgetUpdateTime == null || !widgetUpdateTime.equals(values.localDateTime)) {
            scheduleWidgetUpdate(values.localDateTime);
        }
    }

    public void checkWidgetUpdate() {
        AppSingleton.getInstance(this).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
            if (rozvrhWrapper.getRozvrh() != null) {
                Rozvrh.GetNCLCTreturnValues values = rozvrhWrapper.getRozvrh().getNextCurrentLessonChangeTime();
                if (widgetUpdateTime == null || !widgetUpdateTime.equals(values.localDateTime)) {
                    scheduleWidgetUpdate(values.localDateTime);
                }
            }
        });

    }

    public void scheduleWidgetUpdate(LocalDateTime updateTime) {
        if (updateTime == null) {
            updateTime = LocalDateTime.now().plusHours(6);
        }
        Intent intent = new Intent(this, AppWidgetProvider.class);
        intent.setAction(AppWidgetProvider.ACTION_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, updateTime.toDate().getTime(), pendingIntent);

        Log.d(TAG, "Scheduled a widget upadate on " + updateTime.toString("MM-dd HH:mm:ss"));
    }

    private static PendingIntent getNotiPendingIntent(Context context) {
        Intent intent = new Intent(context, NotiBroadcastReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), NotiBroadcastReciever.REQUEST_CODE, intent, 0);
        return pendingIntent;
    }

    /**
     * Schedules notification update accordingly to the give Rozvrh using
     * {@link Rozvrh#getNextCurrentLessonChangeTime()}. If there is already one scheduled, it is
     * overwritten.
     *
     * @return <code>true</code> if successful or <code>false</code> if not
     * ({@link Rozvrh#getNextCurrentLessonChangeTime()} returned error, because this is an
     * old/permanent schedule).
     */
    public boolean scheduleNotificationUpdate(Rozvrh rozvrh) {
        Rozvrh.GetNCLCTreturnValues values = rozvrh.getNextCurrentLessonChangeTime();
        if (values.localDateTime == null) {
            return false;
        }
        scheduleNotificationUpdate(values.localDateTime);
        return true;
    }

    /**
     * Same as {@link #scheduleNotificationUpdate(Rozvrh)}, but gets the rozvrh for you.
     *
     * @param onFinished
     */
    public void scheduleNotificationUpdate(onFinishedListener onFinished) {
        RozvrhAPI rozvrhAPI = AppSingleton.getInstance(this).getRozvrhAPI();
        rozvrhAPI.getNextNotificationUpdateTime(updateTime -> {
            if (updateTime == null) {
                onFinished.onFinished(false);
            } else {
                scheduleNotificationUpdate(updateTime);
                onFinished.onFinished(true);
            }
        });
    }

    public void enableNotification() {
        SharedPrefs.setBoolean(this, getString(R.string.PREFS_NOTIFICATION), true);
        RozvrhAPI rozvrhAPI = AppSingleton.getInstance(this).getRozvrhAPI();
        if (notificationLiveData != null)
            notificationLiveData.removeObserver(notificationObserver);
        notificationLiveData = rozvrhAPI.getCurrentWeekLiveData();
        notificationLiveData.observeForever(notificationObserver);
        PermanentNotification.update(notificationLiveData.getValue() == null ? null : notificationLiveData.getValue().getRozvrh(), this);
    }

    public void disableNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(getNotiPendingIntent(this));
        SharedPrefs.setBoolean(this, getString(R.string.PREFS_NOTIFICATION), false);
        PermanentNotification.update(null, 0, this);
        if (notificationLiveData != null) {
            notificationLiveData.removeObserver(notificationObserver);
            notificationLiveData = null;
        }
    }

    public static interface onFinishedListener {
        public void onFinished(boolean successful);
    }

    /**
     * Starts up sentry crash reporting, but only if it is an official build and crash reporting is
     * allowed (see build.gradle).
     */
    public void enableSentry() {
        /*
         * Only enable sentry on the official release build
         */
        if (BuildConfig.ALLOW_SENTRY) {
            Sentry.init("https://d13d732d380444f5bed7487cfea65814@sentry.io/1820627", new AndroidSentryClientFactory(this));
            Sentry.getContext().addExtra("commit hash", BuildConfig.GitHash);

            if (!SharedPrefs.contains(this, SharedPrefs.SENTRY_ID) || SharedPrefs.getString(this, SharedPrefs.SENTRY_ID).isEmpty()) {
                SharedPrefs.setString(this, SharedPrefs.SENTRY_ID, "android:" + Long.toHexString(new Random().nextLong()));
            }
            Sentry.getContext().setUser(new User(SharedPrefs.getString(this, SharedPrefs.SENTRY_ID), null, null, null));
        } else {
            diableSentry();
            SharedPrefs.setBooleanPreference(this, R.string.PREFS_SEND_CRASH_REPORTS, false);
        }
    }

    public void diableSentry() {
        Sentry.close();
    }

    @Override
    public void onTerminate() {
        //prevent leaks
        if (notificationLiveData != null) {
            notificationLiveData.removeObserver(notificationObserver);
            notificationLiveData = null;
        }
        super.onTerminate();
    }

}
package cz.vitskalicky.lepsirozvrh;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import cz.vitskalicky.lepsirozvrh.notification.NotificationState;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;
import cz.vitskalicky.lepsirozvrh.widget.AppWidgetProvider;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.User;


public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();

    private NotificationState notificationState = null;
    private LocalDateTime updateTime = null;
    private LiveData<RozvrhWrapper> currentWeekLivedata = null;
    private Observer<RozvrhWrapper> currentWeekObserver = rozvrhWrapper -> {
        if (rozvrhWrapper.getRozvrh() != null) {
            AppWidgetProvider.updateAll(rozvrhWrapper.getRozvrh(), this);
            if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_NOTIFICATION, true)) {
                PermanentNotification.update(rozvrhWrapper.getRozvrh(), this);
            }
            updateUpdateTime(rozvrhWrapper.getRozvrh());
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
        currentWeekLivedata = rozvrhAPI.getCurrentWeekLiveData();
        currentWeekLivedata.observeForever(currentWeekObserver);
    }

    public void scheduleUpdate(LocalDateTime triggerTime){
        if (triggerTime == null) {
            triggerTime = LocalDateTime.now().plusHours(6);
        }
        if (notificationState.getOffsetResetTime() != null && triggerTime.isAfter(notificationState.getOffsetResetTime())) {
            triggerTime = notificationState.getOffsetResetTime();
        }
        Intent intent = new Intent(this, UpdateBroadcastReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, UpdateBroadcastReciever.REQUEST_CODE, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime.toDate().getTime(), 60 * 60000, pendingIntent);

        Log.d(TAG, "Scheduled an update on " + triggerTime.toString("MM-dd HH:mm:ss"));
        updateTime = triggerTime;
    }

    public NotificationState getNotificationState() {
        return notificationState;
    }

    public void updateUpdateTime(Rozvrh rozvrh) {
        if (rozvrh == null){
            return;
        }
        Rozvrh.GetNCLCTreturnValues values = rozvrh.getNextCurrentLessonChangeTime();
        if (values.localDateTime != null && (updateTime == null || updateTime.isAfter(values.localDateTime))) {
            scheduleUpdate(values.localDateTime);
        }
    }

    public void updateUpdateTime() {
        if (currentWeekLivedata != null && currentWeekLivedata.getValue() != null){
            updateUpdateTime(currentWeekLivedata.getValue().getRozvrh());
        }
    }

    public void enableNotification() {
        SharedPrefs.setBoolean(this, getString(R.string.PREFS_NOTIFICATION), true);
        RozvrhAPI rozvrhAPI = AppSingleton.getInstance(this).getRozvrhAPI();
        if (currentWeekLivedata != null){
            PermanentNotification.update(currentWeekLivedata.getValue() == null ? null : currentWeekLivedata.getValue().getRozvrh(), this);
        }
    }

    public void disableNotification() {
        SharedPrefs.setBoolean(this, getString(R.string.PREFS_NOTIFICATION), false);
        PermanentNotification.update(null, 0, this);
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
        if (currentWeekLivedata != null) {
            currentWeekLivedata.removeObserver(currentWeekObserver);
            currentWeekLivedata = null;
        }
        super.onTerminate();
    }

}
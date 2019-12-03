package cz.vitskalicky.lepsirozvrh;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.LocalDateTime;

import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.notification.NotiBroadcastReciever;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;


public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Sentry (crash report) client
        if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_SEND_CRASH_REPORTS)){
            enableSentry();
        }else{
            diableSentry();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Register notification channel for the permanent notification
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(PermanentNotification.PERMANENT_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.silence),new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
            channel.setShowBadge(false);
            channel.setVibrationPattern(null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        scheduleNotificationUpdate(LocalDateTime.now().plusMinutes(1));
        PermanentNotification.update(this, AppSingleton.getInstance(this).getRozvrhAPI(), () -> {});
    }

    /**
     * Schedules AlarmManager for notification update
     * @param triggerTime
     */
    public void scheduleNotificationUpdate(LocalDateTime triggerTime){
        Intent intent = new Intent(this, NotiBroadcastReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), NotiBroadcastReciever.REQUEST_CODE, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime.toDate().getTime(),10 * 60000,  pendingIntent);

        Log.d(TAG, "Scheduled a notificatio upadate on " + triggerTime.toString("MM-dd HH:mm:ss"));
    }

    /**
     * Schedules notification update accordingly to the give Rozvrh using
     * {@link Rozvrh#getNextCurrentLessonChangeTime()}. If there is already one scheduled, it is
     * overwritten.
     * @return <code>true</code> if successful or <code>false</code> if not
     * ({@link Rozvrh#getNextCurrentLessonChangeTime()} returned error, because this is an
     * old/permanent schedule).
     */
    public boolean scheduleNotificationUpdate(Rozvrh rozvrh){
        Rozvrh.GetNCLCTreturnValues values = rozvrh.getNextCurrentLessonChangeTime();
        if (values.localDateTime == null){
            return false;
        }
        scheduleNotificationUpdate(values.localDateTime);
        return true;
    }

    /**
     * Same as {@link #scheduleNotificationUpdate(Rozvrh)}, but gets the rozvrh fro you.
     * @param onFinished
     */
    public void scheduleNotificationUpdate(onFinishedListener onFinished){
        RozvrhAPI rozvrhAPI = AppSingleton.getInstance(this).getRozvrhAPI();
        rozvrhAPI.justGet(Utils.getCurrentMonday(), (code, rozvrh) -> {
            if (rozvrh == null || !scheduleNotificationUpdate(rozvrh)){
                rozvrhAPI.justGet(Utils.getCurrentMonday().plusWeeks(1), (code1, rozvrh1) -> {
                    onFinished.onFinished(rozvrh1 != null && scheduleNotificationUpdate(rozvrh1));
                });
            }else {
                onFinished.onFinished(true);
            }
        });
    }

    public static interface onFinishedListener {
        public void onFinished(boolean successful);
    }

    public void enableSentry(){
        Sentry.init("https://d13d732d380444f5bed7487cfea65814@sentry.io/1820627", new AndroidSentryClientFactory(this));
    }

    public void diableSentry(){
        Sentry.close();
    }
}
package cz.vitskalicky.lepsirozvrh.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.RemoteInput;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;

/**
 * Stands for <b>Noti</b>fication <b>broadcast reciever</b>
 */
public class NotiBroadcastReciever extends BroadcastReceiver {
    private static final String TAG = NotiBroadcastReciever.class.getSimpleName();
    public static final int REQUEST_CODE = 64857;
    /**
     * +1 for next, -1 for prev
     */
    public static final String EXTRA_NEXT_PREV = "extra-next-or-prev-lesson";

    Context context;
    RozvrhAPI rozvrhAPI = null;
    MainApplication application;
    PendingResult pendingResult;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast recieved");
        this.context = context;
        rozvrhAPI = AppSingleton.getInstance(context).getRozvrhAPI();
        application = (MainApplication) context.getApplicationContext();
        pendingResult = goAsync();

        if (intent.hasExtra(EXTRA_NEXT_PREV)){
            int offset = intent.getIntExtra(EXTRA_NEXT_PREV, 0);
            application.getNotificationState().setOffset(application.getNotificationState().getOffset() + offset);
        }

        PermanentNotification.update(application, rozvrhAPI, () -> {
            pendingResult.finish();
        });
    }
}

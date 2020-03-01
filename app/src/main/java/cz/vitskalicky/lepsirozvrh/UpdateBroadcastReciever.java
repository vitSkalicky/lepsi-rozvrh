package cz.vitskalicky.lepsirozvrh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;
import cz.vitskalicky.lepsirozvrh.widget.WidgetProvider;

/**
 * Broadcast receiver that updates notification and widgets when receives a broadcast.
 */
public class UpdateBroadcastReciever extends BroadcastReceiver {
    private static final String TAG = UpdateBroadcastReciever.class.getSimpleName();
    public static final int REQUEST_CODE = 64857;
    /**
     * +1 for next, -1 for prev
     */
    public static final String EXTRA_NEXT_PREV = BuildConfig.APPLICATION_ID + ".extra-next-or-prev-lesson";
    public static final String ACTION_NEXT_PREV = BuildConfig.APPLICATION_ID + ".action-next-or-prev-lesson";

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

        if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_NEXT_PREV) && intent.hasExtra(EXTRA_NEXT_PREV)){
            int offset = intent.getIntExtra(EXTRA_NEXT_PREV, 0);
            application.getNotificationState().setOffset(application.getNotificationState().getOffset() + offset);
            application.scheduleUpdate(application.getNotificationState().getOffsetResetTime());
        }

        rozvrhAPI.getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
            PermanentNotification.update(rozvrhWrapper.getRozvrh(), application);
            WidgetProvider.updateAll(rozvrhWrapper.getRozvrh(), context);

            application.updateUpdateTime(pendingResult::finish);
        });
    }
}

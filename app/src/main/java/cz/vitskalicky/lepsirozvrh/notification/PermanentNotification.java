package cz.vitskalicky.lepsirozvrh.notification;

import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.core.app.TaskStackBuilder;
import androidx.core.text.HtmlCompat;

import java.util.List;

import cz.vitskalicky.lepsirozvrh.BuildConfig;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.activity.MainActivity;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class PermanentNotification {
    public static final int PERMANENT_NOTIFICATION_ID = 7055713;
    public static final String PERMANENT_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".permanentNotificationChannel";
    public static final String PREF_DONT_SHOW_INFO_DIALOG = "dont-show-notification-info-dialog-again";
    public static final String EXTRA_NOTIFICATION = PermanentNotification.class.getCanonicalName() + "-extra-notification";

    /**
     * Same as {@link #update(RozvrhHodina, Context)}, but gets the RozvrhHodina for you.
     * @param onFinished called when finished (Rozvrh may be fetched from the internet).
     */
    public static void update(MainApplication application, RozvrhAPI rozvrhAPI, Utils.Listener onFinished){
        Context context = application;
        if (!SharedPrefs.getBooleanPreference(context, R.string.PREFS_NOTIFICATION, true)){
            update(null,0, context);
            onFinished.method();
            return;
        }
        rozvrhAPI.justGet(Utils.getDisplayWeekMonday(context), (code, rozvrh) -> {
            if (rozvrh != null){
                Rozvrh.GetNLreturnValues nextLessonInfo = rozvrh.getHighlightLesson(true);
                int offset = application.getNotificationState().getOffset();
                RozvrhHodina rozvrhHodina = nextLessonInfo == null ? null : nextLessonInfo.rozvrhHodina;
                if (rozvrhHodina == null){
                    update(rozvrhHodina,0, context);
                }else {
                    List<RozvrhHodina> hodiny = rozvrh.getDny().get(nextLessonInfo.dayIndex).getHodiny();
                    int hodinaIndex = nextLessonInfo.lessonIndex + offset;
                    if (hodinaIndex < 0 || hodinaIndex > hodiny.size() - 1){
                        rozvrhHodina = null;
                    }else {
                        rozvrhHodina = hodiny.get(hodinaIndex);
                    }
                    update(rozvrhHodina, offset, context);
                }
                application.scheduleNotificationUpdate(successful -> {
                    onFinished.method();
                });
            } else {
                onFinished.method();
            }
        });
    }

    /**
     * Updates the notification with the data of supplied RozvrhHodina. (Notification is hidden if
     * RozvrhHodina is null)
     */
    public static void update(RozvrhHodina hodina,int offset, Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if ((hodina == null && offset == 0) || !SharedPrefs.getBooleanPreference(context, R.string.PREFS_NOTIFICATION, true)) {
            notificationManager.cancel(PERMANENT_NOTIFICATION_ID);
            return;
        }

        String offsetText = "";
        String predmet = "";
        String mistnost = "";
        String ucitel = "";
        String skupina = "";
        if (hodina == null){
            predmet = context.getString(R.string.nothing);
        }else {
        predmet = hodina.getPr();
        if (predmet == null || predmet.isEmpty())
            predmet = hodina.getZkrpr();
        if (predmet == null || predmet.isEmpty())
            predmet = hodina.getZkratka();
        if (predmet == null || predmet.isEmpty())
            predmet = hodina.getNazev();
        if (predmet == null || predmet.isEmpty())
            predmet = "";

        mistnost = hodina.getMist();
        if (mistnost == null || mistnost.isEmpty())
            mistnost = hodina.getZkrmist();
        if (mistnost == null || mistnost.isEmpty())
            mistnost = "";

        ucitel = hodina.getUc();
        if (ucitel == null || ucitel.isEmpty())
            ucitel = hodina.getZkruc();
        if (ucitel == null || ucitel.isEmpty())
            ucitel = "";

        skupina = hodina.getSkup();
        if (skupina == null || skupina.isEmpty())
            skupina = hodina.getZkrskup();
        if (skupina == null || skupina.isEmpty())
            skupina = "";
        else //skupina is not empty
            skupina = context.getString(R.string.group_in_notification) + " " + skupina;
        }
        if (offset != 0){
            offsetText = offset + ": ";
        }

        CharSequence title = "";
        if (!predmet.isEmpty() && !mistnost.isEmpty()) {
            title = HtmlCompat.fromHtml(predmet + " - <b>" + mistnost + "</b>", HtmlCompat.FROM_HTML_MODE_COMPACT);
        } else {
            title = HtmlCompat.fromHtml(predmet + "<b>" + mistnost + "</b>", HtmlCompat.FROM_HTML_MODE_COMPACT);
        }
        if (!offsetText.isEmpty()){
            title = offsetText + title;
        }

        CharSequence content = "";
        if (!ucitel.isEmpty() && !skupina.isEmpty()) {
            content = ucitel + ", " + skupina;
        } else {
            content = ucitel + skupina;
        }

        Intent nextIntent = new Intent(context, NotiBroadcastReciever.class);
        nextIntent.putExtra(NotiBroadcastReciever.EXTRA_NEXT_PREV, 1);
        PendingIntent nextPendingIntent =
                PendingIntent.getBroadcast(context, 0, nextIntent, 0);

        Intent prevIntent = new Intent(context, NotiBroadcastReciever.class);
        prevIntent.putExtra(NotiBroadcastReciever.EXTRA_NEXT_PREV, -1);
        PendingIntent prevPendingIntent =
                PendingIntent.getBroadcast(context, 0, prevIntent, 0);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_JUMP_TO_TODAY, true);
        intent.putExtra(EXTRA_NOTIFICATION, true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PERMANENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setWhen(Long.MAX_VALUE)
                .setShowWhen(false)
                .setSound(Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.silence))
                .setVibrate(new long[]{})
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.pre,"dalsimore", nextPendingIntent)
                .addAction(0, "predchozi more", prevPendingIntent);
        Notification ntf = builder.build();

        // notificationId is a unique int for each notification that you must
        notificationManager.notify(PERMANENT_NOTIFICATION_ID, ntf);
    }

    public static void showInfoDialog(Context context, boolean ignoreSetting){
        if (!ignoreSetting && SharedPrefs.getBoolean(context, PREF_DONT_SHOW_INFO_DIALOG)){
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.notification);
        View contentView = LayoutInflater.from(context).inflate(R.layout.notification_dialog,null);
        CheckBox checkBox = contentView.findViewById(R.id.checkBox);
        builder.setView(contentView);
        builder.setPositiveButton(android.R.string.yes,(dialog, which) -> {
            SharedPrefs.setBoolean(context, PREF_DONT_SHOW_INFO_DIALOG, checkBox.isChecked());
        });
        builder.show();
    }
}

package cz.vitskalicky.lepsirozvrh.notification;

import android.app.Application;
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
import androidx.core.app.TaskStackBuilder;
import androidx.core.text.HtmlCompat;

import org.joda.time.format.DateTimeFormat;

import java.util.List;

import cz.vitskalicky.lepsirozvrh.BuildConfig;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.activity.MainActivity;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;
import io.sentry.util.Util;

public class PermanentNotification {
    public static final int PERMANENT_NOTIFICATION_ID = 7055713;
    public static final String PERMANENT_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".permanentNotificationChannel";
    public static final String PREF_DONT_SHOW_INFO_DIALOG = "dont-show-notification-info-dialog-again";
    public static final String EXTRA_NOTIFICATION = PermanentNotification.class.getCanonicalName() + "-extra-notification";

    public static void update(RozvrhAPI rozvrhAPI, MainApplication application, Utils.Listener onFinished){
        Context context = application;
        if (!SharedPrefs.getBooleanPreference(context, R.string.PREFS_NOTIFICATION, true)){
            update(null,0, context);
            return;
        }
        rozvrhAPI.getRozvrh(Utils.getDisplayWeekMonday(context), rozvrhWrapper -> {
            update(rozvrhWrapper.getRozvrh(), application);
            onFinished.method();
        });
    }

    /**
     * Same as {@link #update(RozvrhHodina, int, Context)}, but gets the RozvrhHodina for you.
     */
    public static void update(Rozvrh rozvrh, MainApplication application){
        Context context = application;
        if (!SharedPrefs.getBooleanPreference(context, R.string.PREFS_NOTIFICATION, true)){
            update(null,0, context);
            return;
        }
        if (rozvrh != null){
            Rozvrh.GetNLreturnValues nextLessonInfo = rozvrh.getHighlightLesson(true);
            int offset = application.getNotificationState().getOffset();
            RozvrhHodina rozvrhHodina = nextLessonInfo == null ? null : nextLessonInfo.rozvrhHodina;
            if (rozvrhHodina == null){
                update(null,0, context);
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
            application.scheduleNotificationUpdate(rozvrh);
        }

    }

    /**
     * Updates the notification with the data of supplied RozvrhHodina. (Notification is hidden if
     * RozvrhHodina is null)
     */
    public static void update(RozvrhHodina hodina,int offset, Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        boolean isTeacher = Login.isTeacher(context);

        if ((hodina == null && offset == 0) || !SharedPrefs.getBooleanPreference(context, R.string.PREFS_NOTIFICATION, true)) {
            notificationManager.cancel(PERMANENT_NOTIFICATION_ID);
            return;
        }

        String offsetText = "";
        String predmet = "";
        String mistnost = "";
        String ucitel = "";
        String skupina = "";
        String cas = "";
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

            if (predmet.isEmpty()){
                if(hodina.getHighlight() == RozvrhHodina.CHANGED){
                    predmet = context.getString(R.string.lesson_cancelled);
                }else if (hodina.getHighlight() != RozvrhHodina.NONE){
                    predmet = context.getString(R.string.nothing);
                }
            }

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

            if (isTeacher){
                // in teacher's schedule the class name is saved in skup and zkrskup
                // and we want to display it in the place where the teacher's name would usually be.
                ucitel = skupina;
                skupina = "";
            }

            if (!skupina.isEmpty()) {
                skupina = context.getString(R.string.group_in_notification) + " " + skupina;
            }


            String beginTime = hodina.getParsedBegintime() == null ? "" : hodina.getParsedBegintime().toString(DateTimeFormat.shortTime());
            String endTime = hodina.getEndtime() == null ? "" : hodina.getParsedEndtime().toString(DateTimeFormat.shortTime());
            if (!beginTime.isEmpty() && !endTime.isEmpty()){
                cas = beginTime + " - " + endTime;
            }
        }
        if (offset != 0){
            offsetText = offset + ": ";
            if (offset > 0){
                offsetText = "+" + offsetText;
            }
        }

        CharSequence title = "";
        if (!predmet.isEmpty() && !mistnost.isEmpty()) {
            title = HtmlCompat.fromHtml(offsetText + predmet + " " + context.getString(R.string.in) + " <b>" + mistnost + "</b>", HtmlCompat.FROM_HTML_MODE_COMPACT);
        } else{
            title = HtmlCompat.fromHtml(offsetText + predmet + "<b>" + mistnost + "</b>", HtmlCompat.FROM_HTML_MODE_COMPACT);
        }
        /*if (!offsetText.isEmpty()){
            title = offsetText + title;
        }*/

        CharSequence content = "";
        String contentString = "";
        if (!ucitel.isEmpty() && !skupina.isEmpty()) {
            contentString = ucitel + ", " + skupina;
        } else {
            contentString = ucitel + skupina;
        }
        if (!contentString.isEmpty() && !cas.isEmpty()){
            contentString = contentString + ", " + cas;
        }else {
            contentString = contentString + cas;
        }
        content = contentString;

        CharSequence expanded = content;
        if (!mistnost.isEmpty()){
            expanded = expanded + ", " + context.getString(R.string.room) + ": " + mistnost;
        }

        Intent nextIntent = new Intent(context, NotiBroadcastReciever.class);
        nextIntent.setAction(NotiBroadcastReciever.ACTION_NEXT_PREV);
        nextIntent.putExtra(NotiBroadcastReciever.EXTRA_NEXT_PREV, 1);
        PendingIntent nextPendingIntent =
                PendingIntent.getBroadcast(context, 458631, nextIntent, 0);

        Intent prevIntent = new Intent(context, NotiBroadcastReciever.class);
        prevIntent.setAction(NotiBroadcastReciever.ACTION_NEXT_PREV);
        prevIntent.putExtra(NotiBroadcastReciever.EXTRA_NEXT_PREV, -1);
        PendingIntent prevPendingIntent =
                PendingIntent.getBroadcast(context, 4586, prevIntent,0);

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
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(expanded))
                .addAction(R.drawable.ic_navigate_before_black_24dp, context.getString(R.string.prev_lesson), prevPendingIntent)
                .addAction(R.drawable.ic_navigate_next_black_24dp,context.getString(R.string.next_lesson), nextPendingIntent)
                ;
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

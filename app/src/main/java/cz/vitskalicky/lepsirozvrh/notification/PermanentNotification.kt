package cz.vitskalicky.lepsirozvrh.notification

import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.text.HtmlCompat
import cz.vitskalicky.lepsirozvrh.*
import cz.vitskalicky.lepsirozvrh.activity.MainActivity
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhWrapper
import cz.vitskalicky.lepsirozvrh.model.relations.BlockRelated
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson
import org.joda.time.format.DateTimeFormat

object PermanentNotification {
    const val PERMANENT_NOTIFICATION_ID = 7055713
    const val PERMANENT_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".permanentNotificationChannel"
    const val PREF_DONT_SHOW_INFO_DIALOG = "dont-show-notification-info-dialog-again"
    public val EXTRA_NOTIFICATION = PermanentNotification::class.java.canonicalName + "-extra-notification"

    suspend fun update(application: MainApplication) {
        if (!SharedPrefs.getBooleanPreference(application, R.string.PREFS_NOTIFICATION, true)) {
            update(null, 0, application)
            return
        }
        application.repository.getRozvrh(Utils.getCurrentMonday()).let {
            update(it, application)
        }
    }

    /**
     * Same as [update], but gets the RozvrhHodina for you.
     */
    fun update(rozvrh: RozvrhRelated?, application: MainApplication) {
        val context: Context = application
        if (!SharedPrefs.getBooleanPreference(context, R.string.PREFS_NOTIFICATION, true)) {
            update(null, 0, context)
            return
        }
        if (rozvrh != null) {
            val block = rozvrh.getHighlightBlock(true)
            val offset = application.notificationState.offset
            if (block == null) {
                update(null, 0, context)
            } else {
                val hodiny = rozvrh.days.find { it.day.date == block.block.day }?.blocks
                val hodinaIndex = block.caption.index + offset
                val newBlock = hodiny?.getOrNull(hodinaIndex)
                update(newBlock, offset, context)
            }
        } else {
            if (!(context.applicationContext as MainApplication).login!!.isLoggedIn) {
                update(null, 0, context)
            }
        }
    }

    /**
     * Updates the notification with the data of the first lesson of supplied [BlockRelated]. If there are no lesson "no lesson" text in notification is showed. If [block] is `null`, the notification is hidden.
     */
    fun update(block: BlockRelated?, offset: Int, context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        val isTeacher = (context.applicationContext as MainApplication).login?.isTeacher == true
        if (block == null && offset == 0 || !SharedPrefs.getBooleanPreference(context, R.string.PREFS_NOTIFICATION, true)) {
            notificationManager.cancel(PERMANENT_NOTIFICATION_ID)
            return
        }
        var offsetText = ""
        var predmet: String = ""
        var mistnost: String = ""
        var ucitel: String = ""
        var skupina: String = ""
        var cas = ""

        val lesson = block?.block?.lessons?.firstOrNull();
        if (block == null || lesson == null) {
            predmet = context.getString(R.string.nothing)
        } else {
            predmet = lesson.subjectName.ifBlank { lesson.subjectAbbrev }

            if (predmet.isBlank()) {
                if (lesson.changeType != RozvrhLesson.NO_CHANGE) {
                    predmet = context.getString(R.string.lesson_cancelled)
                } else {
                    predmet = context.getString(R.string.nothing)
                }
            }
            mistnost = lesson.roomName.ifBlank { lesson.roomAbbrev }
            ucitel = lesson.teacherName.ifBlank { lesson.teacherAbbrev }
            skupina = lesson.groups.joinToString(", ") { it.name.ifBlank { it.abbrev } }
            if (isTeacher) {
                // in teacher's schedule the class name is saved in skup and zkrskup
                // and we want to display it in the place where the teacher's name would usually be.
                ucitel = skupina
                skupina = ""
            }
            if (skupina.isNotBlank()) {
                skupina = context.getString(R.string.group_in_notification, skupina)
            }
            val beginTime = block.caption.beginTime.toString(DateTimeFormat.shortTime())
            val endTime = block.caption.endTime.toString(DateTimeFormat.shortTime())
            if (beginTime.isNotBlank() && endTime.isNotBlank()) {
                cas = "$beginTime - $endTime"
            }
        }
        if (offset != 0) {
            offsetText = "$offset: "
            if (offset > 0) {
                offsetText = "+$offsetText"
            }
        }
        var title: CharSequence = ""
        title = if (!predmet.isBlank() && !mistnost.isBlank()) {
            HtmlCompat.fromHtml(offsetText + predmet + " " + context.getString(R.string.`in`) + " <b>" + mistnost + "</b>", HtmlCompat.FROM_HTML_MODE_COMPACT)
        } else {
            HtmlCompat.fromHtml("$offsetText$predmet<b>$mistnost</b>", HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
        /*if (!offsetText.isEmpty()){
            title = offsetText + title;
        }*/
        var content: CharSequence? = ""
        var contentString = ""
        contentString = if (!ucitel!!.isEmpty() && !skupina!!.isEmpty()) {
            "$ucitel, $skupina"
        } else {
            ucitel + skupina
        }
        contentString = if (!contentString.isEmpty() && !cas.isEmpty()) {
            "$contentString, $cas"
        } else {
            contentString + cas
        }
        content = contentString
        var expanded: CharSequence = content
        if (!mistnost!!.isEmpty()) {
            expanded = expanded.toString() + ", " + context.getString(R.string.room) + " " + mistnost
        }
        val nextIntent = Intent(context, UpdateBroadcastReciever::class.java)
        nextIntent.action = UpdateBroadcastReciever.ACTION_NEXT_PREV
        nextIntent.putExtra(UpdateBroadcastReciever.EXTRA_NEXT_PREV, 1)
        val nextPendingIntent = PendingIntent.getBroadcast(context, 458631, nextIntent, 0)
        val prevIntent = Intent(context, UpdateBroadcastReciever::class.java)
        prevIntent.action = UpdateBroadcastReciever.ACTION_NEXT_PREV
        prevIntent.putExtra(UpdateBroadcastReciever.EXTRA_NEXT_PREV, -1)
        val prevPendingIntent = PendingIntent.getBroadcast(context, 4586, prevIntent, 0)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_JUMP_TO_TODAY, true)
        intent.putExtra(EXTRA_NOTIFICATION, true)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(intent)
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        //create notification
        val builder = NotificationCompat.Builder(context, PERMANENT_CHANNEL_ID)
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
                .setVibrate(longArrayOf())
                .setOnlyAlertOnce(true)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(expanded))
                .addAction(R.drawable.ic_navigate_before_black_24dp, context.getString(R.string.prev_lesson), prevPendingIntent)
                .addAction(R.drawable.ic_navigate_next_black_24dp, context.getString(R.string.next_lesson), nextPendingIntent)
        val ntf = builder.build()

        // notificationId is a unique int for each notification that you must
        notificationManager.notify(PERMANENT_NOTIFICATION_ID, ntf)
    }

    fun showInfoDialog(context: Context?, ignoreSetting: Boolean) {
        if (!ignoreSetting && SharedPrefs.getBoolean(context, PREF_DONT_SHOW_INFO_DIALOG)) {
            return
        }
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.notification)
        val contentView = LayoutInflater.from(context).inflate(R.layout.notification_dialog, null)
        val checkBox = contentView.findViewById<CheckBox>(R.id.checkBox)
        builder.setView(contentView)
        builder.setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, which: Int -> SharedPrefs.setBoolean(context, PREF_DONT_SHOW_INFO_DIALOG, checkBox.isChecked) }
        builder.show()
    }
}
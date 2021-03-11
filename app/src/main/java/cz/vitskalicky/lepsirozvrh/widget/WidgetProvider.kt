package cz.vitskalicky.lepsirozvrh.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import cz.vitskalicky.lepsirozvrh.AppSingleton
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.R
import cz.vitskalicky.lepsirozvrh.UpdateBroadcastReciever
import cz.vitskalicky.lepsirozvrh.activity.MainActivity
import cz.vitskalicky.lepsirozvrh.model.relations.BlockRelated
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson
import cz.vitskalicky.lepsirozvrh.widget.WidgetsSettings.Widget

open class WidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "Updating widgets")
        val widgetsSettings = AppSingleton.getInstance(context).widgetsSettings
        var somethingAdded = false
        for (id in appWidgetIds) {
            if (widgetsSettings.widgetIds.add(id)) {
                somethingAdded = true
                val settings = Widget()
                settings.primaryTextColor = ContextCompat.getColor(context, R.color.widgetLightPrimaryText)
                settings.secondaryTextColor = ContextCompat.getColor(context, R.color.widgetLightSecondaryText)
                settings.primaryTextSize = context.resources.getDimensionPixelSize(R.dimen.widgetTextPrimary) / context.resources.displayMetrics.scaledDensity
                settings.secondaryTextSize = context.resources.getDimensionPixelSize(R.dimen.widgetTextSecondary) / context.resources.displayMetrics.scaledDensity
                settings.backgroundColor = ContextCompat.getColor(context, R.color.widgetLightBackground)
                widgetsSettings.widgets[id] = settings
            }
        }
        if (somethingAdded) {
            AppSingleton.getInstance(context).saveWidgetsSettings()
        }
        val updateIntent = Intent(context, UpdateBroadcastReciever::class.java)
        context.sendBroadcast(updateIntent)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val widgetsSettings = AppSingleton.getInstance(context).widgetsSettings
        for (id in appWidgetIds) {
            widgetsSettings.widgets.remove(id)
            widgetsSettings.widgetIds.remove(id)
        }
        AppSingleton.getInstance(context).saveWidgetsSettings()
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        Log.d(TAG, "Updating widget options")
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val widgetsSettings = AppSingleton.getInstance(context).widgetsSettings
        if (widgetsSettings.widgetIds.add(appWidgetId)) {
            val settings = Widget()
            settings.primaryTextColor = ContextCompat.getColor(context, R.color.widgetLightPrimaryText)
            settings.secondaryTextColor = ContextCompat.getColor(context, R.color.widgetLightSecondaryText)
            settings.primaryTextSize = context.resources.getDimensionPixelSize(R.dimen.widgetTextPrimary) / context.resources.displayMetrics.scaledDensity
            settings.secondaryTextSize = context.resources.getDimensionPixelSize(R.dimen.widgetTextSecondary) / context.resources.displayMetrics.scaledDensity
            settings.backgroundColor = ContextCompat.getColor(context, R.color.widgetLightBackground)
            widgetsSettings.widgets[appWidgetId] = settings
            AppSingleton.getInstance(context).saveWidgetsSettings()
        }
        val updateIntent = Intent(context, UpdateBroadcastReciever::class.java)
        context.sendBroadcast(updateIntent)
    }

    companion object {
        val TAG = WidgetProvider::class.java.simpleName
        const val PENDING_INTENT_REQUEST_CODE = 85321
        fun updateAll(rozvrh: RozvrhRelated?, context: Context) {
            val widgetsSettings = AppSingleton.getInstance(context).widgetsSettings
            val display: Pair<List<BlockRelated>?,String?>? = rozvrh?.getWidgetDisplayBlocks(5)
            val widgetIds = widgetsSettings.widgetIds
            for (id in widgetIds) {
                update(context, id, display?.first,display?.second)
            }
        }

        /**
         * Updates the widget with the given id. If there is an event today, leave [hodiny] `null` and put the event name into [event]. Otherwise leave [event] `null`.
         */
        fun update(context: Context, widgetID: Int, hodiny: List<BlockRelated>?, event: String? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            var widgetSettings = AppSingleton.getInstance(context).widgetsSettings.widgets[widgetID]
            var loggedIn: Boolean = (context.applicationContext as MainApplication).login.isLoggedIn()

            // failsafe
            if (widgetSettings == null) {
                widgetSettings = Widget()
                Log.e(TAG, "There are widget settings missing for widget with id $widgetID")
            }
            val allEmpty:Boolean = hodiny.isNullOrEmpty()

            val options = appWidgetManager.getAppWidgetOptions(widgetID)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            val views: RemoteViews
            if (!loggedIn) {
                views = RemoteViews(context.packageName, R.layout.small_widget)
                views.setTextViewText(R.id.textViewZkrpr, "")
                views.setViewVisibility(R.id.textViewZkrpr, View.GONE)
                views.setTextViewText(R.id.textViewSecondary, context.getString(R.string.widget_logged_out))
                views.setInt(R.id.textViewZkrpr, "setTextColor", widgetSettings.primaryTextColor)
                views.setInt(R.id.textViewSecondary, "setTextColor", widgetSettings.secondaryTextColor)
                views.setFloat(R.id.textViewZkrpr, "setTextSize", widgetSettings.primaryTextSize)
                views.setFloat(R.id.textViewSecondary, "setTextSize", widgetSettings.secondaryTextSize)
            } else if (width < 250 || allEmpty || !event.isNullOrBlank()) {
                views = RemoteViews(context.packageName, R.layout.small_widget)
                val hodina: RozvrhLesson? = hodiny?.firstOrNull()?.block?.lessons?.firstOrNull()
                updateCell(views, R.id.textViewZkrpr, R.id.textViewSecondary, hodina, event, widgetSettings, false, context)
            } else {
                views = RemoteViews(context.packageName, R.layout.wide_widget)

                val lesson: List<RozvrhLesson?> = hodiny!!.map { it.block.lessons.firstOrNull() }

                updateCell(views, R.id.textViewZkrpr0, R.id.textViewSecondary0, lesson.getOrNull(0), null,widgetSettings, true, context)
                updateCell(views, R.id.textViewZkrpr1, R.id.textViewSecondary1, lesson.getOrNull(1), null,widgetSettings, true, context)
                updateCell(views, R.id.textViewZkrpr2, R.id.textViewSecondary2, lesson.getOrNull(2), null,widgetSettings, true, context)
                updateCell(views, R.id.textViewZkrpr3, R.id.textViewSecondary3, lesson.getOrNull(3), null,widgetSettings, true, context)
                updateCell(views, R.id.textViewZkrpr4, R.id.textViewSecondary4, lesson.getOrNull(4), null,widgetSettings, true, context)
                views.setInt(R.id.imageViewDivider, "setImageAlpha", 255)
                views.setInt(R.id.imageViewDivider, "setColorFilter", widgetSettings.primaryTextColor)
            }
            views.setInt(R.id.bgcolor, "setImageAlpha", widgetSettings.backgroundColor and -0x1000000 shr 24)
            views.setInt(R.id.bgcolor, "setColorFilter", widgetSettings.backgroundColor or -0x1000000)
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_JUMP_TO_TODAY, true)
            val pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE, intent, 0)
            views.setOnClickPendingIntent(R.id.root, pendingIntent)
            appWidgetManager.updateAppWidget(widgetID, views)
        }

        private fun updateCell(views: RemoteViews, primaryTextId: Int, secondaryTextId: Int, hodina: RozvrhLesson?, event: String?, settings: Widget, allowEmpty: Boolean, context: Context) {
            if (!event.isNullOrBlank()){
                views.setTextViewText(primaryTextId, "")
                views.setViewVisibility(primaryTextId, View.GONE)
                views.setTextViewText(secondaryTextId, event)

                views.setInt(primaryTextId, "setTextColor", settings.primaryTextColor)
                views.setInt(secondaryTextId, "setTextColor", settings.primaryTextColor) //on event the secondary text has color of primary text
                views.setFloat(primaryTextId, "setTextSize", settings.primaryTextSize)
                views.setFloat(secondaryTextId, "setTextSize", settings.secondaryTextSize)
            }else {
                if (hodina == null) {
                    views.setTextViewText(primaryTextId, "")
                    views.setViewVisibility(primaryTextId, View.GONE)
                    if (allowEmpty) {
                        views.setTextViewText(secondaryTextId, "")
                    } else {
                        views.setTextViewText(secondaryTextId, context.getString(R.string.nothing))
                    }
                } else {
                    var tchr = hodina.teacherAbbrev
                    if ((context.applicationContext as MainApplication).login.isTeacher()) {
                        // to teacher's we want to show the class, not the teacher
                        // the class name is saved in zkrskup and skup
                        tchr = hodina.groups.joinToString(", ") { it.abbrev }
                    }
                    if (hodina.subjectAbbrev.isBlank() && hodina.teacherAbbrev.isBlank() && hodina.changeType != RozvrhLesson.NO_CHANGE) {
                        views.setTextViewText(primaryTextId, "")
                        views.setViewVisibility(primaryTextId, View.GONE)
                        views.setTextViewText(secondaryTextId, context.getString(R.string.lesson_cancelled))
                    } else {
                        views.setTextViewText(primaryTextId, hodina.subjectAbbrev)
                        views.setViewVisibility(primaryTextId, View.VISIBLE)
                        views.setTextViewText(secondaryTextId, buildSpannedString {
                            append(tchr)
                            append(" ")
                            bold { append(hodina.roomAbbrev) }
                        })
                    }
                }
                views.setInt(primaryTextId, "setTextColor", settings.primaryTextColor)
                views.setInt(secondaryTextId, "setTextColor", settings.secondaryTextColor)
                views.setFloat(primaryTextId, "setTextSize", settings.primaryTextSize)
                views.setFloat(secondaryTextId, "setTextSize", settings.secondaryTextSize)
            }
        }
    }
}
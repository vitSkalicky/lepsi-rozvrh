package cz.vitskalicky.lepsirozvrh.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import java.util.HashSet;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.activity.MainActivity;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class WidgetProvider extends android.appwidget.AppWidgetProvider {

    public static final String TAG = WidgetProvider.class.getSimpleName();
    public static final int PENDING_INTENT_REQUEST_CODE = 85321;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    public static void updateAll(Rozvrh rozvrh, Context context) {
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        RozvrhHodina[] hodiny = rozvrh == null ? null : rozvrh.getWidgetDiaplayValues(5);

        HashSet<Integer> widgetIds = widgetsSettings.widgetIds;
        for (int id : widgetIds) {
            update(id, hodiny, context);
        }
    }

    public static void update(int widgetID, RozvrhHodina[] hodiny, Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        WidgetsSettings.Widget widgetSettings = AppSingleton.getInstance(context).getWidgetsSettings().widgets.get(widgetID);

        boolean loggedOut = false;

        // check login
        if (Login.getToken(context).isEmpty()){
            //logged out
            loggedOut = true;
        }

        // failsafe
        if (widgetSettings == null){
            widgetSettings = new WidgetsSettings.Widget();
            Log.e(TAG,"There are widget settings missing for widget id " + widgetID);
        }

        boolean allEmpty = true;
        if (hodiny == null) {
            allEmpty = true;
        } else {
            for (RozvrhHodina item : hodiny) {
                if (item != null && !item.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
        }

        Bundle options = appWidgetManager.getAppWidgetOptions(widgetID);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);

        RemoteViews views;

        if (loggedOut){
            views = new RemoteViews(context.getPackageName(), R.layout.small_widget);

            views.setTextViewText(R.id.textViewZkrpr, "");
            views.setViewVisibility(R.id.textViewZkrpr, View.GONE);
            views.setTextViewText(R.id.textViewSecondary, context.getString(R.string.widget_logged_out));
            views.setInt(R.id.textViewZkrpr, "setTextColor", widgetSettings.primaryTextColor);
            views.setInt(R.id.textViewSecondary, "setTextColor", widgetSettings.secondaryTextColor);
            views.setFloat(R.id.textViewZkrpr, "setTextSize", widgetSettings.primaryTextSize);
            views.setFloat(R.id.textViewSecondary, "setTextSize", widgetSettings.secondaryTextSize);

        } else if (width < 250 || allEmpty) {
            views = new RemoteViews(context.getPackageName(), R.layout.small_widget);

            RozvrhHodina hodina;
            if (allEmpty || hodiny.length < 1 || hodiny[0] == null || hodiny[0].getHighlight() == RozvrhHodina.EMPTY) {
                hodina = null;
            } else {
                hodina = hodiny[0];
            }

            updateCell(views, R.id.textViewZkrpr, R.id.textViewSecondary, hodina, widgetSettings, false, context);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.wide_widget);

            if (hodiny.length < 5) {
                RozvrhHodina[] tmp = new RozvrhHodina[5];
                //noinspection ManualArrayCopy
                for (int i = 0; i < hodiny.length; i++) {
                    tmp[i] = hodiny[i];
                }
                hodiny = tmp;
            }

            updateCell(views, R.id.textViewZkrpr0, R.id.textViewSecondary0, hodiny[0], widgetSettings, true, context);
            updateCell(views, R.id.textViewZkrpr1, R.id.textViewSecondary1, hodiny[1], widgetSettings, true, context);
            updateCell(views, R.id.textViewZkrpr2, R.id.textViewSecondary2, hodiny[2], widgetSettings, true, context);
            updateCell(views, R.id.textViewZkrpr3, R.id.textViewSecondary3, hodiny[3], widgetSettings, true, context);
            updateCell(views, R.id.textViewZkrpr4, R.id.textViewSecondary4, hodiny[4], widgetSettings, true, context);


            views.setInt(R.id.imageViewDivider, "setImageAlpha", 255);
            views.setInt(R.id.imageViewDivider, "setColorFilter", widgetSettings.primaryTextColor);
        }

        views.setInt(R.id.bgcolor, "setImageAlpha", 255);
        views.setInt(R.id.bgcolor, "setColorFilter", widgetSettings.backgroundColor);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_JUMP_TO_TODAY, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_REQUEST_CODE, intent, 0);

        views.setOnClickPendingIntent(R.id.root, pendingIntent);

        appWidgetManager.updateAppWidget(widgetID, views);
    }

    private static void updateCell(RemoteViews views, int primaryTextId, int secondaryTextId, RozvrhHodina hodina, WidgetsSettings.Widget settings, boolean allowEmpty, Context context) {
        if (hodina == null) {
            views.setTextViewText(primaryTextId, "");
            views.setViewVisibility(primaryTextId, View.GONE);
            if (allowEmpty) {
                views.setTextViewText(secondaryTextId, "");
            } else {
                views.setTextViewText(secondaryTextId, context.getString(R.string.nothing));
            }
        } else {
            String zkrpr = hodina.getZkrpr();
            if (zkrpr == null || zkrpr.isEmpty())
                zkrpr = hodina.getZkratka();
            if (zkrpr == null)
                zkrpr = "";
            String zkrmist = hodina.getZkrmist();
            if (zkrmist == null)
                zkrmist = "";
            String zkruc = hodina.getZkruc();
            if (zkruc == null)
                zkruc = "";
            if (Login.isTeacher(context)) {
                // to teacher's we want to show the class, not the teacher
                // the class name is saved in zkrskup and skup
                zkruc = hodina.getZkrskup();
                if (zkruc == null || zkruc.isEmpty()) {
                    zkruc = hodina.getSkup();
                }
                if (zkruc == null) {
                    zkruc = "";
                }
            }

            if (zkrpr.isEmpty() && zkruc.isEmpty() && hodina.getHighlight() == RozvrhHodina.CHANGED) {
                views.setTextViewText(primaryTextId, "");
                views.setViewVisibility(primaryTextId, View.GONE);
                views.setTextViewText(secondaryTextId, context.getString(R.string.lesson_cancelled));
            } else {
                views.setTextViewText(primaryTextId, zkrpr);
                views.setViewVisibility(primaryTextId, View.VISIBLE);
                views.setTextViewText(secondaryTextId, HtmlCompat.fromHtml(zkruc + " <b>" + zkrmist + "</b>", HtmlCompat.FROM_HTML_MODE_COMPACT));
            }
        }

        views.setInt(primaryTextId, "setTextColor", settings.primaryTextColor);
        views.setInt(secondaryTextId, "setTextColor", settings.secondaryTextColor);
        views.setFloat(primaryTextId, "setTextSize", settings.primaryTextSize);
        views.setFloat(secondaryTextId, "setTextSize", settings.secondaryTextSize);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        PendingResult pendingResult = goAsync();

        boolean somethingAdded = false;

        for (int id : appWidgetIds) {
            if (widgetsSettings.widgetIds.add(id)) {
                somethingAdded = true;
                WidgetsSettings.Widget settings = new WidgetsSettings.Widget();
                settings.primaryTextColor = ContextCompat.getColor(context, R.color.widgetLightPrimaryText);
                settings.secondaryTextColor = ContextCompat.getColor(context, R.color.widgetLightSecondaryText);
                settings.primaryTextSize = context.getResources().getDimensionPixelSize(R.dimen.widgetTextPrimary) / context.getResources().getDisplayMetrics().scaledDensity;
                settings.secondaryTextSize = context.getResources().getDimensionPixelSize(R.dimen.widgetTextSecondary) / context.getResources().getDisplayMetrics().scaledDensity;
                settings.backgroundColor = ContextCompat.getColor(context, R.color.widgetLightBackground);
                widgetsSettings.widgets.put(id, settings);
            }
        }
        if (somethingAdded) {
            AppSingleton.getInstance(context).saveWidgetsSettings();
        }

        AppSingleton.getInstance(context).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
            Rozvrh rozvrh = rozvrhWrapper.getRozvrh();
            updateAll(rozvrh, context);
            ((MainApplication) context.getApplicationContext()).updateUpdateTime(rozvrh);
            pendingResult.finish();
        });
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        for (int id : appWidgetIds) {
            widgetsSettings.widgets.remove(id);
            widgetsSettings.widgetIds.remove(id);
        }

        AppSingleton.getInstance(context).saveWidgetsSettings();
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        PendingResult pendingResult = goAsync();

        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();
        if (widgetsSettings.widgetIds.add(appWidgetId)) {
            WidgetsSettings.Widget settings = new WidgetsSettings.Widget();
            settings.primaryTextColor = ContextCompat.getColor(context, R.color.widgetLightPrimaryText);
            settings.secondaryTextColor = ContextCompat.getColor(context, R.color.widgetLightSecondaryText);
            settings.primaryTextSize = context.getResources().getDimensionPixelSize(R.dimen.widgetTextPrimary) / context.getResources().getDisplayMetrics().scaledDensity;
            settings.secondaryTextSize = context.getResources().getDimensionPixelSize(R.dimen.widgetTextSecondary) / context.getResources().getDisplayMetrics().scaledDensity;
            settings.backgroundColor = ContextCompat.getColor(context, R.color.widgetLightBackground);
            widgetsSettings.widgets.put(appWidgetId, settings);
            AppSingleton.getInstance(context).saveWidgetsSettings();
        }

        AppSingleton.getInstance(context).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
            Rozvrh rozvrh = rozvrhWrapper.getRozvrh();
            update(appWidgetId, rozvrh == null ? null : rozvrh.getWidgetDiaplayValues(5), context);
            ((MainApplication) context.getApplicationContext()).updateUpdateTime(rozvrh);
            pendingResult.finish();
        });
    }
}

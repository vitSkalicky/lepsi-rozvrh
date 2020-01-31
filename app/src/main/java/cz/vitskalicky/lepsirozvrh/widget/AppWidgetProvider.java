package cz.vitskalicky.lepsirozvrh.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import java.util.HashSet;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.activity.MainActivity;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    public static void updateAll(Rozvrh rozvrh, Context context) {
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        Rozvrh.WidgetValues values;
        if (rozvrh == null) {
            values = new Rozvrh.WidgetValues(); //these are empty
            values.denIndex = -2;//this means error
        } else {
            values = rozvrh.getWidgetDiaplayValues();
        }
        RozvrhHodina[] hodiny = new RozvrhHodina[5];
        if (values.denIndex < 0 || values.lessonIndex < 0 || values.lessonIndex == Integer.MAX_VALUE) {
            hodiny = null;
        } else {
            List<RozvrhHodina> denHodiny = rozvrh.getDny().get(values.denIndex).getHodiny();
            for (int i = 0; i < hodiny.length && i + values.lessonIndex < denHodiny.size(); i++) {
                hodiny[i] = denHodiny.get(values.lessonIndex + i);
            }
        }
        HashSet<Integer> widgetIds = widgetsSettings.widgetIds;
        for (int id : widgetIds) {
            update(id, hodiny, context);
        }
    }

    public static void update(int widgetID, RozvrhHodina[] hodiny, Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        Bundle options = appWidgetManager.getAppWidgetOptions(widgetID);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);


        if (width < 400 || hodiny == null) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            
            RozvrhHodina hodina;
            if (hodiny == null || hodiny.length < 1 || hodiny[0] == null || hodiny[0].getHighlight() == RozvrhHodina.EMPTY) {
                hodina = null;
            } else {
                hodina = hodiny[0];
            }

            updateCell(views, R.id.bgcolor, R.id.textViewZkrpr, R.id.textViewSecondary, hodina, widgetsSettings.widgets.get(widgetID), false, context);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_JUMP_TO_TODAY, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.root, pendingIntent);

            appWidgetManager.updateAppWidget(widgetID, views);
        } else {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_wide);

            WidgetsSettings.Widged item = widgetsSettings.widgets.get(widgetID);

            if (hodiny.length < 5) {
                RozvrhHodina[] tmp = new RozvrhHodina[5];
                for (int i = 0; i < hodiny.length; i++) {
                    tmp[i] = hodiny[i];
                }
                hodiny = tmp;
            }
            
            updateCell(views, R.id.bgcolor, R.id.textViewZkrpr0, R.id.textViewSecondary0, hodiny[0], item, true, context);
            updateCell(views, R.id.bgcolor, R.id.textViewZkrpr1, R.id.textViewSecondary1, hodiny[1], item, true, context);
            updateCell(views, R.id.bgcolor, R.id.textViewZkrpr2, R.id.textViewSecondary2, hodiny[2], item, true, context);
            updateCell(views, R.id.bgcolor, R.id.textViewZkrpr3, R.id.textViewSecondary3, hodiny[3], item, true, context);
            updateCell(views, R.id.bgcolor, R.id.textViewZkrpr4, R.id.textViewSecondary4, hodiny[4], item, true, context);


            views.setInt(R.id.imageViewDivider0, "setImageAlpha", 255);
            views.setInt(R.id.imageViewDivider0, "setColorFilter", item.primaryTextColor);
            views.setInt(R.id.bgcolor, "setImageAlpha", 255);
            views.setInt(R.id.bgcolor, "setColorFilter", item.backgroundColor);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_JUMP_TO_TODAY, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.root, pendingIntent);

            appWidgetManager.updateAppWidget(widgetID, views);
        }
    }

    private static void updateCell(RemoteViews views, int bgcolorId, int primaryTextId, int secondaryTextId, RozvrhHodina hodina, WidgetsSettings.Widged settings, boolean allowEmpty, Context context) {
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
        views.setInt(bgcolorId, "setImageAlpha", 255);
        views.setInt(bgcolorId, "setColorFilter", settings.backgroundColor);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        PendingResult pendingResult = goAsync();

        boolean somethingAdded = false;

        for (int id : appWidgetIds) {
            if (widgetsSettings.widgetIds.add(id)) {
                somethingAdded = true;
                WidgetsSettings.Widged settings = new WidgetsSettings.Widged();
                settings.primaryTextColor = ContextCompat.getColor(context, R.color.widgetLightPrimaryText);
                settings.secondaryTextColor = ContextCompat.getColor(context, R.color.widgetLightSecondaryText);
                settings.primaryTextSize = context.getResources().getDimensionPixelSize(R.dimen.widgetTextPrimary) / context.getResources().getDisplayMetrics().scaledDensity;
                settings.secondaryTextSize = context.getResources().getDimensionPixelSize(R.dimen.widgetTextSecondary) / context.getResources().getDisplayMetrics().scaledDensity;
                settings.backgroundColor = ContextCompat.getColor(context, R.color.widgetLightBackground);
                widgetsSettings.widgets.put(id, settings);
            }
        }
        if (somethingAdded) {
            AppSingleton.getInstance(context).saveWidgetSettings();
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

        AppSingleton.getInstance(context).saveWidgetSettings();
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        PendingResult pendingResult = goAsync();

        AppSingleton.getInstance(context).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
            Rozvrh rozvrh = rozvrhWrapper.getRozvrh();
            updateAll(rozvrh, context);
            ((MainApplication) context.getApplicationContext()).updateUpdateTime(rozvrh);
            pendingResult.finish();
        });
    }
}

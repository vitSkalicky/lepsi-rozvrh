package cz.vitskalicky.lepsirozvrh.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
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
        System.out.println("pada");
    }

    public static void update(Rozvrh rozvrh, Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        Rozvrh.WidgetValues values;
        if (rozvrh == null){
            values = new Rozvrh.WidgetValues(); //these are empty
            values.denIndex = -2;//this means error
        }else {
            values = rozvrh.getWidgetDiaplayValues();
        }
        if (values.denIndex < 0 || values.lessonIndex < 0 || values.lessonIndex == Integer.MAX_VALUE){
            //empty
            HashSet<Integer> widgetIds = widgetsSettings.widgetIds;
            for (int id :widgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
                views.setTextViewText(R.id.textViewZkrpr, "");
                views.setViewVisibility(R.id.textViewZkrpr,View.GONE);
                if (values.denIndex == -2){
                    views.setTextViewText(R.id.textViewSecondary, context.getString(R.string.info_offline));
                }else {
                    views.setTextViewText(R.id.textViewSecondary, context.getString(R.string.nothing));
                }
                appWidgetManager.updateAppWidget(id, views);
            }
        }else {
            RozvrhHodina hodina = rozvrh.getDny().get(values.denIndex).getHodiny().get(values.lessonIndex);
            HashSet<Integer> widgetIds = widgetsSettings.widgetIds;
            for (int id :widgetIds) {

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
                if (Login.isTeacher(context)){
                    // to teacher's we want to show the class, not the teacher
                    // the class name is saved in zkrskup and skup
                    zkruc = hodina.getZkrskup();
                    if (zkruc == null || zkruc.isEmpty()){
                        zkruc = hodina.getSkup();
                    }
                    if (zkruc == null){
                        zkruc = "";
                    }
                }

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
                views.setTextViewText(R.id.textViewZkrpr, zkrpr);
                views.setViewVisibility(R.id.textViewZkrpr,View.VISIBLE);
                views.setTextViewText(R.id.textViewSecondary, HtmlCompat.fromHtml(zkruc + " <b>" + zkrmist + "</b>", HtmlCompat.FROM_HTML_MODE_COMPACT));
                appWidgetManager.updateAppWidget(id, views);
            }
        }
    }

    public static void updateLooks(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        for (int id :widgetsSettings.widgets.keySet()) {
            WidgetsSettings.Widged item = widgetsSettings.widgets.get(id);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_JUMP_TO_TODAY, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            views.setOnClickPendingIntent(R.id.root, pendingIntent);

            views.setInt(R.id.textViewZkrpr, "setTextColor", item.primaryTextColor);
            views.setInt(R.id.textViewSecondary, "setTextColor", item.secondaryTextColor);
            views.setFloat(R.id.textViewZkrpr, "setTextSize", item.primaryTextSize);
            views.setFloat(R.id.textViewSecondary, "setTextSize", item.secondaryTextSize);
            views.setInt(R.id.root,"setBackgroundColor", item.backgroundColor);


            appWidgetManager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        PendingResult pendingResult = goAsync();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                null,
                R.styleable.Rozvrh,
                0, R.style.AppTheme);

        boolean somethingAdded = false;

        for (int id : appWidgetIds) {
            if (widgetsSettings.widgetIds.add(id)){
                somethingAdded = true;
                WidgetsSettings.Widged settings = new WidgetsSettings.Widged();
                settings.primaryTextColor = a.getColor(R.styleable.Rozvrh_textPrimaryColor, Color.BLACK);
                settings.secondaryTextColor =  a.getColor(R.styleable.Rozvrh_textSecondaryColor, Color.BLACK);
                settings.primaryTextSize = 16;
                settings.secondaryTextSize = 12;
                settings.backgroundColor = Color.WHITE;
                widgetsSettings.widgets.put(id, settings);
            }
        }
        if (somethingAdded){
            AppSingleton.getInstance(context).saveWidgetSettings();
        }
        updateLooks(context);

        AppSingleton.getInstance(context).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
            Rozvrh rozvrh = rozvrhWrapper.getRozvrh();
            update(rozvrh, context);
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
}

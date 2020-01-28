package cz.vitskalicky.lepsirozvrh.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.activity.MainActivity;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {
    public static final String ACTION_UPDATE = AppWidgetProvider.class.getCanonicalName() + ".UPDATE_ALL_WIDGETS";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())){
            PendingResult pendingResult = goAsync();

            AppSingleton.getInstance(context).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
                update(rozvrhWrapper.getRozvrh(), context);
                pendingResult.finish();
            });
        }
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
        RozvrhHodina hodina;
        if (values.denIndex < 0 || values.lessonIndex < 0 || values.lessonIndex == Integer.MAX_VALUE){
            hodina = null;
        }else {
            hodina = rozvrh.getDny().get(values.denIndex).getHodiny().get(values.lessonIndex);
        }
        HashSet<Integer> widgetIds = widgetsSettings.widgetIds;
        for (int id :widgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);

            if (hodina != null){
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

                // update content
                views.setTextViewText(R.id.textViewZkrpr, zkrpr);
                views.setViewVisibility(R.id.textViewZkrpr,View.VISIBLE);
                views.setTextViewText(R.id.textViewSecondary, HtmlCompat.fromHtml(zkruc + " <b>" + zkrmist + "</b>", HtmlCompat.FROM_HTML_MODE_COMPACT));
            }else {
                views.setTextViewText(R.id.textViewZkrpr, "");
                views.setViewVisibility(R.id.textViewZkrpr,View.GONE);
                if (values.denIndex == -2){
                    views.setTextViewText(R.id.textViewSecondary, context.getString(R.string.info_offline));
                }else {
                    views.setTextViewText(R.id.textViewSecondary, context.getString(R.string.nothing));
                }
            }
            // update style
            WidgetsSettings.Widged item = widgetsSettings.widgets.get(id);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_JUMP_TO_TODAY, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.root, pendingIntent);

            views.setInt(R.id.textViewZkrpr, "setTextColor", item.primaryTextColor);
            views.setInt(R.id.textViewSecondary, "setTextColor", item.secondaryTextColor);
            views.setFloat(R.id.textViewZkrpr, "setTextSize", item.primaryTextSize);
            views.setFloat(R.id.textViewSecondary, "setTextSize", item.secondaryTextSize);
            views.setInt(R.id.bgcolor, "setImageAlpha", 255);
            views.setInt(R.id.bgcolor, "setColorFilter",  item.backgroundColor);


            appWidgetManager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetsSettings widgetsSettings = AppSingleton.getInstance(context).getWidgetsSettings();

        PendingResult pendingResult = goAsync();

        boolean somethingAdded = false;

        for (int id : appWidgetIds) {
            if (widgetsSettings.widgetIds.add(id)){
                somethingAdded = true;
                WidgetsSettings.Widged settings = new WidgetsSettings.Widged();
                settings.primaryTextColor = ContextCompat.getColor(context, R.color.widgetLightPrimaryText);
                settings.secondaryTextColor = ContextCompat.getColor(context, R.color.widgetLightSecondaryText);
                settings.primaryTextSize = context.getResources().getDimensionPixelSize(R.dimen.widgetTextPrimary) / context.getResources().getDisplayMetrics().scaledDensity;
                settings.secondaryTextSize =  context.getResources().getDimensionPixelSize(R.dimen.widgetTextSecondary) / context.getResources().getDisplayMetrics().scaledDensity;
                settings.backgroundColor = ContextCompat.getColor(context, R.color.widgetLightBackground);
                widgetsSettings.widgets.put(id, settings);
            }
        }
        if (somethingAdded){
            AppSingleton.getInstance(context).saveWidgetSettings();
        }

        AppSingleton.getInstance(context).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
            Rozvrh rozvrh = rozvrhWrapper.getRozvrh();
            update(rozvrh, context);
            ((MainApplication) context.getApplicationContext()).checkWidgetUpdate(rozvrh);
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

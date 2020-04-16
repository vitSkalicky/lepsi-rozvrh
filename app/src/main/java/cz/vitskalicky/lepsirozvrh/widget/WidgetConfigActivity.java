package cz.vitskalicky.lepsirozvrh.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.core.content.ContextCompat;

import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;

/**
 * A base class for Widget configuration activities taking care of saving the data, OK, button and spinner.
 * A subclass should override {@link #setupContentView()} to provide content view and
 * {@link #onStyleSet(int)} to change the appearance of displayed preview.
 */
public class WidgetConfigActivity extends CyaneaAppCompatActivity {
    private static final String TAG = WidgetConfigActivity.class.getSimpleName();

    Spinner spinner;
    Button buttonOK;

    int widgetID = 0;
    boolean isWidgetIDSet = false;

    int style = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupContentView();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setStyle(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            isWidgetIDSet = true;
        }

        buttonOK.setOnClickListener(v -> {
            saveConfig();
            if (isWidgetIDSet) {
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                setResult(RESULT_OK, resultValue);
                AppSingleton.getInstance(this).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
                    WidgetProvider.update(widgetID, rozvrhWrapper.getRozvrh() == null ? null : rozvrhWrapper.getRozvrh().getWidgetDiaplayValues(5), this);
                    finish();
                });
            } else {
                finish();
            }

        });

    }

    /**
     * A subclass should override this and set the {@link #spinner} and {@link #buttonOK}.
     */
    protected void setupContentView() {
        //This is just a fail-safe
        // !! THIS CODE SHOULD NOT BE EXECUTED !!
        setContentView(R.layout.activity_small_widget_config);

        spinner = findViewById(R.id.spinner);
        buttonOK = findViewById(R.id.buttonOK);

        Log.e(TAG, "Hey, someone forgot to override WidgetConfig#setupContentView !");
    }

    @Override
    public void onBackPressed() {
        if (isWidgetIDSet) {
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_CANCELED, resultValue);
        }
        finish();
    }

    /**
     * @param style 0 = light; 1 = dark
     */
    public void setStyle(int style) {
        this.style = style;
        onStyleSet(style);
    }

    /**
     * Override this to change the appearance of preview.
     */
    protected void onStyleSet(int style) {

    }

    public void saveConfig() {
        if (isWidgetIDSet) {
            WidgetsSettings.Widget ws = new WidgetsSettings.Widget();

            ws.primaryTextSize = getResources().getDimension(R.dimen.widgetTextPrimary) / getResources().getDisplayMetrics().scaledDensity;
            ws.secondaryTextSize = getResources().getDimension(R.dimen.widgetTextSecondary) / getResources().getDisplayMetrics().scaledDensity;

            if (style == 0) {
                ws.primaryTextColor = ContextCompat.getColor(this, R.color.widgetLightPrimaryText);
                ws.secondaryTextColor = ContextCompat.getColor(this, R.color.widgetLightSecondaryText);
                ws.backgroundColor = ContextCompat.getColor(this, R.color.widgetLightBackground);
            } else {
                ws.primaryTextColor = ContextCompat.getColor(this, R.color.widgetDarkPrimaryText);
                ws.secondaryTextColor = ContextCompat.getColor(this, R.color.widgetDarkSecondaryText);
                ws.backgroundColor = ContextCompat.getColor(this, R.color.widgetDarkBackground);
            }

            AppSingleton appSingleton = AppSingleton.getInstance(this);

            appSingleton.getWidgetsSettings().widgetIds.add(widgetID);
            appSingleton.getWidgetsSettings().widgets.put(widgetID, ws);

            appSingleton.saveWidgetsSettings();
        }
    }
}

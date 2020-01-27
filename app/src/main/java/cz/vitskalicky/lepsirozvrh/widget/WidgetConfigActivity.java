package cz.vitskalicky.lepsirozvrh.widget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;

public class WidgetConfigActivity extends AppCompatActivity {

    Spinner spinner;
    Button buttonOK;

    ImageView bgColor;
    TextView textViewPrimary;
    TextView textViewSecondary;

    int widgetID = 0;
    boolean isWidgetIDSet = false;
    
    int style = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        spinner = findViewById(R.id.spinner);
        buttonOK = findViewById(R.id.buttonOK);

        bgColor = findViewById(R.id.bgcolor);
        textViewPrimary = findViewById(R.id.textViewZkrpr);
        textViewSecondary = findViewById(R.id.textViewSecondary);

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
                    AppWidgetProvider.update(rozvrhWrapper.getRozvrh(), this);
                    finish();
                });
            } else {
                finish();
            }

        });

    }

    @Override
    public void onBackPressed() {
        if (isWidgetIDSet){
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_CANCELED, resultValue);
        }
        finish();
    }

    /**
     *
     * @param style 0 = light; 1 = dark
     */
    private void setStyle(int style){
        if (style == 0){
            textViewPrimary.setTextColor(ContextCompat.getColor(this, R.color.widgetLightPrimaryText));
            textViewSecondary.setTextColor(ContextCompat.getColor(this, R.color.widgetLightSecondaryText));
            textViewPrimary.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));
            
            bgColor.setImageAlpha(255);
            bgColor.setColorFilter(ContextCompat.getColor(this, R.color.widgetLightBackground));
        } else if (style == 1){
            textViewPrimary.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkPrimaryText));
            textViewSecondary.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkSecondaryText));
            textViewPrimary.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.widgetTextPrimary));
            textViewSecondary.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            bgColor.setImageAlpha(255);
            bgColor.setColorFilter(ContextCompat.getColor(this, R.color.widgetDarkBackground));
        }
        this.style = style;
    }

    private void saveConfig(){
        if (isWidgetIDSet){
            WidgetsSettings.Widged ws = new WidgetsSettings.Widged();

            ws.primaryTextSize = getResources().getDimension(R.dimen.widgetTextPrimary) / getResources().getDisplayMetrics().scaledDensity;
            ws.secondaryTextSize = getResources().getDimension(R.dimen.widgetTextSecondary) / getResources().getDisplayMetrics().scaledDensity;
            
            if (style == 0){
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

            appSingleton.saveWidgetSettings();
        }
    }
}

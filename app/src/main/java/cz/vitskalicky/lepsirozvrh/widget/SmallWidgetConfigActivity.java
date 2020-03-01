package cz.vitskalicky.lepsirozvrh.widget;

import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import cz.vitskalicky.lepsirozvrh.R;

public class SmallWidgetConfigActivity extends WidgetConfigActivity {

    TextView textViewPrimary;
    TextView textViewSecondary;
    ImageView bgColor;

    @Override
    protected void setupContentView() {
        setContentView(R.layout.activity_small_widget_config);

        spinner = findViewById(R.id.spinner);
        buttonOK = findViewById(R.id.buttonOK);

        textViewPrimary = findViewById(R.id.textViewZkrpr);
        textViewSecondary = findViewById(R.id.textViewSecondary);
        bgColor = findViewById(R.id.bgcolor);
    }

    @Override
    protected void onStyleSet(int style) {
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
    }
}

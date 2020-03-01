package cz.vitskalicky.lepsirozvrh.widget;

import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import cz.vitskalicky.lepsirozvrh.R;

public class WideWidgetConfigActivity extends WidgetConfigActivity {

    TextView textViewPrimary0;
    TextView textViewSecondary0;
    TextView textViewPrimary1;
    TextView textViewSecondary1;
    TextView textViewPrimary2;
    TextView textViewSecondary2;
    TextView textViewPrimary3;
    TextView textViewSecondary3;
    TextView textViewPrimary4;
    TextView textViewSecondary4;
    
    ImageView bgColor;
    ImageView divider;

    @Override
    protected void setupContentView() {
        setContentView(R.layout.activity_wide_widget_config);

        spinner = findViewById(R.id.spinner);
        buttonOK = findViewById(R.id.buttonOK);

        textViewPrimary0 = findViewById(R.id.textViewZkrpr0);
        textViewSecondary0 = findViewById(R.id.textViewSecondary0);
        textViewPrimary1 = findViewById(R.id.textViewZkrpr1);
        textViewSecondary1 = findViewById(R.id.textViewSecondary1);
        textViewPrimary2 = findViewById(R.id.textViewZkrpr2);
        textViewSecondary2 = findViewById(R.id.textViewSecondary2);
        textViewPrimary3 = findViewById(R.id.textViewZkrpr3);
        textViewSecondary3 = findViewById(R.id.textViewSecondary3);
        textViewPrimary4 = findViewById(R.id.textViewZkrpr4);
        textViewSecondary4 = findViewById(R.id.textViewSecondary4);
        
        bgColor = findViewById(R.id.bgcolor);
        divider = findViewById(R.id.imageViewDivider);
    }

    @Override
    protected void onStyleSet(int style) {
        if (style == 0){
            textViewPrimary0.setTextColor(ContextCompat.getColor(this, R.color.widgetLightPrimaryText));
            textViewSecondary0.setTextColor(ContextCompat.getColor(this, R.color.widgetLightSecondaryText));
            textViewPrimary0.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary0.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            textViewPrimary1.setTextColor(ContextCompat.getColor(this, R.color.widgetLightPrimaryText));
            textViewSecondary1.setTextColor(ContextCompat.getColor(this, R.color.widgetLightSecondaryText));
            textViewPrimary1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            textViewPrimary2.setTextColor(ContextCompat.getColor(this, R.color.widgetLightPrimaryText));
            textViewSecondary2.setTextColor(ContextCompat.getColor(this, R.color.widgetLightSecondaryText));
            textViewPrimary2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            textViewPrimary3.setTextColor(ContextCompat.getColor(this, R.color.widgetLightPrimaryText));
            textViewSecondary3.setTextColor(ContextCompat.getColor(this, R.color.widgetLightSecondaryText));
            textViewPrimary3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            textViewPrimary4.setTextColor(ContextCompat.getColor(this, R.color.widgetLightPrimaryText));
            textViewSecondary4.setTextColor(ContextCompat.getColor(this, R.color.widgetLightSecondaryText));
            textViewPrimary4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            bgColor.setImageAlpha(255);
            bgColor.setColorFilter(ContextCompat.getColor(this, R.color.widgetLightBackground));
            divider.setImageAlpha(255);
            divider.setColorFilter(ContextCompat.getColor(this, R.color.widgetLightPrimaryText));

        } else if (style == 1){

            textViewPrimary0.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkPrimaryText));
            textViewSecondary0.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkSecondaryText));
            textViewPrimary0.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary0.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            textViewPrimary1.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkPrimaryText));
            textViewSecondary1.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkSecondaryText));
            textViewPrimary1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            textViewPrimary2.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkPrimaryText));
            textViewSecondary2.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkSecondaryText));
            textViewPrimary2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            textViewPrimary3.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkPrimaryText));
            textViewSecondary3.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkSecondaryText));
            textViewPrimary3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            textViewPrimary4.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkPrimaryText));
            textViewSecondary4.setTextColor(ContextCompat.getColor(this, R.color.widgetDarkSecondaryText));
            textViewPrimary4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
            textViewSecondary4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

            bgColor.setImageAlpha(255);
            bgColor.setColorFilter(ContextCompat.getColor(this, R.color.widgetDarkBackground));
            divider.setImageAlpha(255);
            divider.setColorFilter(ContextCompat.getColor(this, R.color.widgetDarkPrimaryText));
        }
    }
}

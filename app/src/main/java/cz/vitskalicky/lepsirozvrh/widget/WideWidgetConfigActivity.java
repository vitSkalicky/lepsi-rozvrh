package cz.vitskalicky.lepsirozvrh.widget;

import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

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
    protected void createContentView() {
        setContentView(R.layout.activity_wide_widget_config);

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
    public void setBackground(int color) {
        bgColor.setImageAlpha((color & 0xff000000) >> 24);
        bgColor.setColorFilter(color | 0xff000000);
    }

    @Override
    public void setPrimaryTextColor(int color) {
        textViewPrimary0.setTextColor(color);
        textViewPrimary0.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));

        textViewPrimary1.setTextColor(color);
        textViewPrimary1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));

        textViewPrimary2.setTextColor(color);
        textViewPrimary2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));

        textViewPrimary3.setTextColor(color);
        textViewPrimary3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));

        textViewPrimary4.setTextColor(color);
        textViewPrimary4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));

        divider.setImageAlpha(255);
        divider.setColorFilter(color);
    }

    @Override
    public void setSecondaryTextColor(int color) {
        textViewSecondary0.setTextColor(color);
        textViewSecondary0.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

        textViewSecondary1.setTextColor(color);
        textViewSecondary1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

        textViewSecondary2.setTextColor(color);
        textViewSecondary2.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

        textViewSecondary3.setTextColor(color);
        textViewSecondary3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));

        textViewSecondary4.setTextColor(color);
        textViewSecondary4.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));
    }
}

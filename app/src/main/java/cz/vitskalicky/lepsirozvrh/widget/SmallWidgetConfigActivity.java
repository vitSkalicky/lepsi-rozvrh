package cz.vitskalicky.lepsirozvrh.widget;

import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;

public class SmallWidgetConfigActivity extends WidgetConfigActivity {

    TextView textViewPrimary;
    TextView textViewSecondary;
    ImageView bgColor;

    @Override
    protected void createContentView() {
        setContentView(R.layout.activity_small_widget_config);

        textViewPrimary = findViewById(R.id.textViewZkrpr);
        textViewSecondary = findViewById(R.id.textViewSecondary);
        bgColor = findViewById(R.id.bgcolor);
    }

    @Override
    public void setBackground(int color) {
        bgColor.setImageAlpha((color & 0xff000000) >> 24);
        bgColor.setColorFilter(color | 0xff000000);
    }

    @Override
    public void setPrimaryTextColor(int color) {
        textViewPrimary.setTextColor(color);
        textViewPrimary.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextPrimary));
    }

    @Override
    public void setSecondaryTextColor(int color) {
        textViewSecondary.setTextColor(color);
        textViewSecondary.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.widgetTextSecondary));
    }
}

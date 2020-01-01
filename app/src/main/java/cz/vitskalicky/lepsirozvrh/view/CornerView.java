package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import cz.vitskalicky.lepsirozvrh.R;

public class CornerView extends CellView {
    private String text = "";
    private TextPaint myTextPaint;

    public CornerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setDrawDividers(false, false, false);
        backgroundPaint.setColor(a.getColor(R.styleable.Rozvrh_backgroundHeader, Color.BLUE));
        myTextPaint = new TextPaint();
        myTextPaint.setAntiAlias(true);
        myTextPaint.setTextSize(secondaryTextSize);
        myTextPaint.setColor(secondaryTextPaint.getColor());}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null){
            text = "";
        }
        this.text = text;

        invalidate();
        requestLayout();
    }

    @Override
    protected void onDrawContent(Canvas canvas, int xStart, int yStart, int xEnd, int yEnd) {
        int h = yEnd - yStart;
        int w = xEnd - xStart;

        Layout.Alignment alignment = Layout.Alignment.ALIGN_CENTER;
        float spacingMultiplier = 1;
        float spacingAddition = 0;
        boolean includePadding = false;

        StaticLayout myStaticLayout = new StaticLayout(text, myTextPaint, w, alignment, spacingMultiplier, spacingAddition, includePadding);

        float textHeight = myStaticLayout.getHeight();
        float vCenter = (h - textHeight) / 2;

        canvas.save();
        canvas.translate(xStart, yStart + vCenter);
        myStaticLayout.draw(canvas);
        canvas.restore();
    }
}

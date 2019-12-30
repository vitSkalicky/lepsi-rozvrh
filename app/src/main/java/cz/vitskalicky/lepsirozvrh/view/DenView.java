package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;

public class DenView extends CellView {
    private RozvrhDen rozvrhDen = null;
    private String denText = "";
    private String datumText = "";

    public DenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setDrawDividers(true, true, false);
        backgroundPaint.setColor(a.getColor(R.styleable.Rozvrh_backgroundHeader, Color.BLUE));
    }

    @Override
    public int getMinimumWidth() {
        return (int) (super.getMinimumWidth() + Math.max(primaryTextPaint.measureText(denText), secondaryTextPaint.measureText(datumText)));
    }

    @Override
    public int getMinimumHeight() {
        return super.getMinimumHeight() + primaryTextSize + textPadding + secondaryTextSize;
    }

    public int getMinimalComfortableHeight(){
        return ((primaryTextSize / 2) + textPadding + secondaryTextSize) * 2 + super.getMinimumHeight();
    }

    @Override
    protected void onDrawContent(Canvas canvas, int xStart, int yStart, int xEnd, int yEnd) {
        int h = yEnd - yStart;
        int w = xEnd - xStart;

        float actualSecondaryTextSize = datumText.isEmpty() ? 0 : secondaryTextSize;
        float actualPrimaryTextSize = primaryTextSize;

        if (canvas.getHeight() < getMinimumHeight()){
            float overflow = (actualPrimaryTextSize + textPadding + actualSecondaryTextSize) - h;
            if (overflow < 0){
                overflow = 0;
            }
            actualPrimaryTextSize = actualPrimaryTextSize - overflow / ((actualPrimaryTextSize + actualSecondaryTextSize) / actualPrimaryTextSize);
            if (actualSecondaryTextSize > 0){
                actualSecondaryTextSize = actualSecondaryTextSize - overflow / ((primaryTextSize + actualSecondaryTextSize) / actualSecondaryTextSize);
            }
        }
        primaryTextPaint.setTextSize(actualPrimaryTextSize);
        secondaryTextPaint.setTextSize(actualSecondaryTextSize);

        float denBaseline = (h / 2f) + (actualPrimaryTextSize / 2f);
        float middle = (w / 2f);

        float secondaryBaseline = denBaseline + textPadding + actualSecondaryTextSize;

        if (canvas.getHeight() < (getMinimalComfortableHeight() - (secondaryTextSize - actualSecondaryTextSize))){
            //do not align zkrpr to center (vertically)
            //secondary text will be aligned to the bottom and zkrpr to the center of the remaining space
            secondaryBaseline = h;
            denBaseline = (secondaryBaseline - actualSecondaryTextSize) / 2 + (actualPrimaryTextSize/2f);
        }

        primaryTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(denText, middle + xStart, denBaseline + yStart, primaryTextPaint);

        //draw secondary = teacher and room
        secondaryTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(datumText, middle + xStart, secondaryBaseline + yStart, secondaryTextPaint);
    }

    public RozvrhDen getRozvrhDen() {
        return rozvrhDen;
    }

    public void setRozvrhDen(RozvrhDen rozvrhDen) {
        this.rozvrhDen = rozvrhDen;
        if (rozvrhDen != null){
            denText = rozvrhDen.getZkratka();
            if (rozvrhDen.getDatum() == null){
                datumText = "";
            }else{
                DateTimeFormatter dtf = DateTimeFormat.forPattern("d. M.");
                datumText = dtf.print(rozvrhDen.getParsedDatum());
            }
        }else {
            denText = "";
            datumText = "";
        }

        invalidate();
        requestLayout();
    }
}

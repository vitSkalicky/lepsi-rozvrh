package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.theme.Theme;

/**
 * A superclass for views in Rozvrh, taking care of the background, dividers and padding
 */
public class CellView extends View {
    protected Theme t;

    protected Paint backgroundPaint;
    protected Paint dividerPaint;
    protected int dividerWidth;

    protected Paint primaryTextPaint;
    protected int primaryTextSize;
    protected Paint secondaryTextPaint;
    protected int secondaryTextSize;

    protected int paddingTop, paddingRight, paddingBottom, paddingLeft, textPadding;

    protected boolean drawDividerTop, drawDividerCorner, drawDividerLeft;

    public CellView(Context context) {
        this(context, null);
    }

    public CellView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        t = new Theme(context);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(t.getCEmptyBg());

        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(t.getCDivider());
        dividerWidth = t.getPxDividerWidth();
        dividerPaint.setStrokeWidth(dividerWidth);

        primaryTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        primaryTextPaint.setColor(Theme.FALLBACK_COLOR);
        primaryTextSize = t.getPxPrimaryText();
        primaryTextPaint.setTextSize(primaryTextSize);
        primaryTextPaint.setTypeface(Typeface.DEFAULT);

        secondaryTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondaryTextPaint.setColor(Theme.FALLBACK_COLOR);
        secondaryTextSize = t.getPxSecondaryText();
        secondaryTextPaint.setTextSize(secondaryTextSize);
        secondaryTextPaint.setTypeface(Typeface.DEFAULT);

        paddingLeft = t.getPxPaddingLeft();
        paddingTop = t.getPxPaddingTop();
        paddingRight = t.getPxPaddingRight();
        paddingBottom = t.getPxPaddingBottom();
        textPadding = t.getPxTextPadding();

        setDrawDividers(true, true, true);
    }

    /**
     * Enables or disables drawing of dividers
     */
    protected void setDrawDividers(boolean top, boolean corner, boolean left){
        drawDividerTop = top;
        drawDividerCorner = corner;
        drawDividerLeft = left;
    }

    /**
     * @return {@link CellView} returns only the sum of padding and divider width.
     */
    @Override
    public int getMinimumHeight() {
        return (int) (dividerWidth + paddingTop + paddingBottom);
    }

    /**
     * @return {@link CellView} returns only the sum of padding and divider width.
     */
    @Override
    public int getMinimumWidth() {
        return (int) (dividerWidth + paddingLeft + paddingRight);
    }

    /**
     * You probably don't need to override this one.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWS = MeasureSpec.getSize(widthMeasureSpec);
        int specWM = MeasureSpec.getMode(widthMeasureSpec);
        int specHS = MeasureSpec.getSize(heightMeasureSpec);
        int specHM = MeasureSpec.getMode(heightMeasureSpec);

        int w, h;
        if (specHM == MeasureSpec.EXACTLY){
            h = resolveSizeAndState(specHS, heightMeasureSpec,1);
        }else if (specHM == MeasureSpec.UNSPECIFIED || getMinimumHeight() <= specHS){
            h = resolveSizeAndState(getMinimumHeight(), heightMeasureSpec, 1);
        }else {
            h = resolveSizeAndState(specHS, heightMeasureSpec, 1);
        }
        if (specWM == MeasureSpec.EXACTLY){
            w = resolveSizeAndState(specWS, widthMeasureSpec,1);
        }else if (specWM == MeasureSpec.UNSPECIFIED || getMinimumWidth() <= specWS){
            w = resolveSizeAndState(Math.max(getMinimumWidth(), goldenRectangle(MeasureSpec.getSize(h))), widthMeasureSpec, 1);
        }else {
            w = resolveSizeAndState(specWS, widthMeasureSpec, 1);
        }

        setMeasuredDimension(w, h);
    }

    /**
     * What the width for a given height should be so that the cell is a golden rectangle (a rectangle that is nice to the eyes, see wikipedia).
     */
    public static int goldenRectangle(int height){
        double goldenRatio = (1 + Math.sqrt(5))/2d;

        double width =  height / goldenRatio;
        return (int) width;
    }

    /**
     * {@link CellView} draws background, dividers and calculates padding, than calls {@link #onDrawContent(Canvas, int, int, int, int)}.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        if (w == 0 || h == 0){
            return;
        }

        //draw background
        canvas.drawRect(0, 0, w, h, backgroundPaint);

        //# draw dividers
        //left
        if (drawDividerLeft)
            //noinspection SuspiciousNameCombination
            canvas.drawLine((float) dividerWidth / 2, dividerWidth, (float) dividerWidth / 2, h, dividerPaint);

        //top
        if (drawDividerTop)
            canvas.drawLine(dividerWidth, (float) dividerWidth / 2, w, (float) dividerWidth / 2, dividerPaint);

        //corner
        if (drawDividerCorner)
            canvas.drawPoint(dividerWidth/2f,dividerWidth/2f, dividerPaint);

        int xStart = dividerWidth + paddingLeft;
        int yStart = dividerWidth + paddingTop;
        //make sure the size is not negative
        int xEnd = Math.max(w - paddingRight, xStart);
        int yEnd = Math.max(h - paddingBottom, yStart);

        onDrawContent(canvas, xStart,yStart ,xEnd ,yEnd );
    }

    /**
     * custom rozvrh views should draw the texts in this method.
     * @param canvas what to draw on
     * @param xStart where to start (includes padding)
     * @param yStart where to start (includes padding)
     * @param xEnd where to end (includes padding)
     * @param yEnd where to end (includes padding)
     */
    protected void onDrawContent(Canvas canvas, int xStart, int yStart, int xEnd, int yEnd){

    }
}

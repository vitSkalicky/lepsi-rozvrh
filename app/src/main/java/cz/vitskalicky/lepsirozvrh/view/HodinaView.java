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
import androidx.appcompat.app.AlertDialog;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class HodinaView extends View {

    private RozvrhHodina hodina;
    private boolean perm;

    private Paint zkrprPaint;
    private Paint mistPaint;
    private Paint secondaryPaint;
    private Paint dividerPaint;
    private Paint highlightPaint;
    private Paint highlightedDividerPaint;
    private Paint backgroundPaint;

    private int zkrprTextHeight;
    private int mistTextHeight;
    private int secondaryTextHeight;
    private int dividerWidth;
    private int highlightWidth;

    private boolean topHighlighted, leftHighlighted, cornerHighlighted;
    private boolean entireHighlighted; //the highlightening is thecker

    private TypedArray a;

    private int paddingLeft, paddingTop, paddingRight, paddingBottom;

    public float spread = 1;

    public HodinaView(Context context) {
        this(context, null);
    }

    public HodinaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Rozvrh,
                0, R.style.AppTheme);


        zkrprPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zkrprPaint.setColor(a.getColor(R.styleable.Rozvrh_textPrimaryColor, Color.BLACK));
        zkrprTextHeight = a.getDimensionPixelSize(R.styleable.Rozvrh_textPrimarySize, 10);
        zkrprPaint.setTextSize(zkrprTextHeight);
        zkrprPaint.setTypeface(Typeface.DEFAULT);
        zkrprPaint.setTextAlign(Paint.Align.CENTER);

        mistPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mistPaint.setColor(a.getColor(R.styleable.Rozvrh_textStandOutColor, Color.BLACK));
        mistTextHeight = a.getDimensionPixelSize(R.styleable.Rozvrh_textStandOutSize, 10);
        mistPaint.setTextSize(mistTextHeight);
        mistPaint.setTypeface(Typeface.DEFAULT);
        mistPaint.setTextAlign(Paint.Align.RIGHT);

        secondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondaryPaint.setColor(a.getColor(R.styleable.Rozvrh_textSecondaryColor, Color.BLACK));
        secondaryTextHeight = a.getDimensionPixelSize(R.styleable.Rozvrh_textSecondarySize, 10);
        secondaryPaint.setTextSize(secondaryTextHeight);
        secondaryPaint.setTypeface(Typeface.DEFAULT);

        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(a.getColor(R.styleable.Rozvrh_dividerColor, Color.BLACK));
        dividerWidth = a.getDimensionPixelSize(R.styleable.Rozvrh_dividerWidth, 1);
        dividerPaint.setStrokeWidth(dividerWidth);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(a.getColor(R.styleable.Rozvrh_dividerHighlightColor, Color.BLACK));
        highlightWidth = a.getDimensionPixelSize(R.styleable.Rozvrh_dividerHighlightWidth, 2);
        highlightPaint.setStrokeWidth(highlightWidth);

        highlightedDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightedDividerPaint.setColor(a.getColor(R.styleable.Rozvrh_dividerHighlightColor, Color.BLACK));
        highlightedDividerPaint.setStrokeWidth(dividerWidth);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(a.getColor(R.styleable.Rozvrh_backgroundEmpty, Color.WHITE));

        paddingLeft = a.getDimensionPixelSize(R.styleable.Rozvrh_paddingLeft, 2);
        paddingTop = a.getDimensionPixelSize(R.styleable.Rozvrh_paddingTop, 1);
        paddingRight = a.getDimensionPixelSize(R.styleable.Rozvrh_paddingRight, 2);
        paddingBottom = a.getDimensionPixelSize(R.styleable.Rozvrh_paddingBottom, 1);

        setOnClickListener(v -> showDetailDialog());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWS = MeasureSpec.getSize(widthMeasureSpec);
        int specWM = MeasureSpec.getMode(widthMeasureSpec);
        int specHS = MeasureSpec.getSize(heightMeasureSpec);
        int specHM = MeasureSpec.getMode(heightMeasureSpec);

        //TODO: solve the case when the specs would't be EXACTLY (currently, this will never happen)

        int w = resolveSizeAndState(measureMinWidth(), widthMeasureSpec, 1);
        int h = resolveSizeAndState(measureMinHeight(), heightMeasureSpec, 1);

        setMeasuredDimension(w, h);
    }

    public int measureMinWidth() {
        int topText = (int) (paddingLeft + secondaryPaint.measureText("Group") + mistPaint.measureText("room 1") + paddingRight) + 1; //the +1 is just to make there a bit more space
        int middleText = (int) (paddingLeft + zkrprPaint.measureText("MATH") + paddingRight) + 1;
        int bottomText = (int) (paddingLeft + secondaryPaint.measureText("ABCD") + secondaryPaint.measureText("Tchr") + paddingRight) + 1;
        return Math.max(Math.max(topText, middleText), Math.max(bottomText, getSuggestedMinimumWidth()));
    }

    public int measureMinHeight() {
        return Math.max(zkrprTextHeight + Math.max(mistTextHeight, secondaryTextHeight) + secondaryTextHeight + 2 + paddingTop + paddingBottom, getSuggestedMinimumHeight());
    }

    public void update(RozvrhHodina hodina, boolean perm) {
        this.hodina = hodina;
        this.perm = perm;

        if (hodina == null) {
            backgroundPaint.setColor(a.getColor(R.styleable.Rozvrh_backgroundEmpty, Color.WHITE));
        } else if (hodina.getHighlight() == RozvrhHodina.CHANGED) {
            backgroundPaint.setColor(a.getColor(R.styleable.Rozvrh_backgroundChng, Color.RED));
        } else if (hodina.getHighlight() == RozvrhHodina.NO_LESSON) {
            backgroundPaint.setColor(a.getColor(R.styleable.Rozvrh_backgroundA, Color.RED));
        } else if (hodina.getHighlight() == RozvrhHodina.NONE) {
            backgroundPaint.setColor(a.getColor(R.styleable.Rozvrh_backgroundH, Color.WHITE));
        } else if (hodina.getHighlight() == RozvrhHodina.EMPTY) {
            backgroundPaint.setColor(a.getColor(R.styleable.Rozvrh_backgroundEmpty, Color.WHITE));
        }

        invalidate();
        requestLayout();
    }

    public void hightlightEdges(boolean top, boolean left, boolean corner) {
        this.topHighlighted = top;
        this.leftHighlighted = left;
        this.cornerHighlighted = corner;
    }

    public void highlightEntire(boolean highlight) {
        this.entireHighlighted = highlight;
        hightlightEdges(highlight, highlight, highlight);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint paint = dividerPaint;
        int w = getWidth();
        int h = getHeight();

        //draw background
        canvas.drawRect(0, 0, w, h, backgroundPaint);

        //# draw dividers
        //left
        if (leftHighlighted || entireHighlighted) {
            paint = highlightedDividerPaint;
        } else {
            paint = dividerPaint;
        }
        canvas.drawLine((float) dividerWidth / 2, dividerWidth, (float) dividerWidth / 2, h, paint);

        //top
        if (topHighlighted || entireHighlighted) {
            paint = highlightedDividerPaint;
        } else {
            paint = dividerPaint;
        }
        canvas.drawLine(dividerWidth, (float) dividerWidth / 2, w, (float) dividerWidth / 2, paint);

        //corner
        if (cornerHighlighted || entireHighlighted) {
            paint = highlightedDividerPaint;
        } else {
            paint = dividerPaint;
        }
        canvas.drawPoint(dividerWidth/2f,dividerWidth/2f, paint);

        //highlight
        if (entireHighlighted){
            canvas.drawLine(dividerWidth, dividerWidth + (highlightWidth / 2f), w,dividerWidth + (highlightWidth / 2f), highlightPaint);
            canvas.drawLine(w - (highlightWidth / 2f),dividerWidth + (highlightWidth / 2f), w - (highlightWidth / 2f), h - (highlightWidth / 2f), highlightPaint);
            canvas.drawLine(w, h - (highlightWidth / 2f), dividerWidth,h - (highlightWidth / 2f), highlightPaint);
            canvas.drawLine(dividerWidth + (highlightWidth / 2f),h - (highlightWidth / 2f),dividerWidth + (highlightWidth / 2f), dividerWidth + (highlightWidth / 2f), highlightPaint);
        }

        //# draw texts

        if (hodina != null){
            // zkrpr
            // alighn is center
            String zkrpr = hodina.getZkrpr();
            if (zkrpr == null || zkrpr.isEmpty())
                zkrpr = hodina.getZkratka();
            if (zkrpr == null)
                zkrpr = "";
            canvas.drawText(zkrpr, ((w - paddingLeft - paddingRight - dividerWidth) / 2f) + paddingLeft + dividerWidth, ((h - paddingTop - paddingBottom - dividerWidth) / 2f) + paddingTop + dividerWidth + (zkrprTextHeight / 2f), zkrprPaint);

            //zkrmist
            String zkrmist = hodina.getZkrmist();
            if (zkrmist == null)
                zkrmist = "";
            canvas.drawText(zkrmist, w - paddingRight, dividerWidth + paddingTop + mistTextHeight, mistPaint);

            //zkruc
            String zkrskup = hodina.getZkrskup();
            if (zkrskup == null)
                zkrskup = "";
            secondaryPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(zkrskup, dividerWidth + paddingLeft, dividerWidth + paddingTop + secondaryTextHeight, secondaryPaint);

            String zkruc = hodina.getZkruc();
            String cycle = hodina.getCycle();
            if (zkruc == null)
                zkruc = "";
            if (cycle == null)
                cycle = "";
            if (!perm || cycle.isEmpty()){
                secondaryPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(zkruc, ((w - paddingLeft - paddingRight - dividerWidth) / 2f) + paddingLeft + dividerWidth, h - paddingBottom, secondaryPaint);
            }else {
                secondaryPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(cycle, dividerWidth + paddingLeft, h - paddingBottom, secondaryPaint);

                secondaryPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(zkruc, w - paddingRight, h - paddingBottom, secondaryPaint);
            }
        }


    }

    private void addField(StringBuilder sb, int resId, String fieldText){
        if (fieldText != null && !fieldText.trim().equals("")){
            sb.append(getContext().getString(resId)).append(": ").append(fieldText).append("\n");
        }
    }

    public void showDetailDialog(){
        if (hodina == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (hodina.getNazev() != null && !hodina.getNazev().trim().equals("")){
            builder.setTitle(hodina.getNazev());
        }else if (hodina.getPr() != null && !hodina.getPr().trim().equals("")){
            builder.setTitle(hodina.getPr());
        }else if (hodina.getZkrpr() != null && !hodina.getZkrpr().trim().equals("")){
            builder.setTitle(hodina.getZkrpr());
        }

        StringBuilder sb = new StringBuilder();
        addField(sb,R.string.lesson_teacher, hodina.getUc());
        addField(sb,R.string.subject_name, hodina.getPr());
        addField(sb,R.string.lesson_name, hodina.getNazev());
        addField(sb,R.string.room, hodina.getMist());
        addField(sb,R.string.absence, hodina.getAbs());
        addField(sb,R.string.topic, hodina.getTema());
        addField(sb,R.string.room, hodina.getMist());
        addField(sb,R.string.group, hodina.getSkup());
        addField(sb,R.string.change, hodina.getChng());
        addField(sb,R.string.notice, hodina.getNotice());
        addField(sb,R.string.cycle, hodina.getCycle());

        builder.setMessage(sb.toString());
        builder.setPositiveButton(R.string.close, (dialog, which) -> {});

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

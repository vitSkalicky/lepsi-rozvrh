package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

    private int paddingLeft, paddingTop, paddingRight, paddingBottom, textPadding;

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
        mistPaint.setColor(a.getColor(R.styleable.Rozvrh_textRoomColor, Color.BLACK));
        mistTextHeight = a.getDimensionPixelSize(R.styleable.Rozvrh_textSecondarySize, 10);
        mistPaint.setTextSize(mistTextHeight);
        mistPaint.setTypeface(Typeface.DEFAULT);
        mistPaint.setTextAlign(Paint.Align.LEFT);

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
        textPadding = a.getDimensionPixelSize(R.styleable.Rozvrh_textPadding,1);

        setOnClickListener(v -> showDetailDialog());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWS = MeasureSpec.getSize(widthMeasureSpec);
        int specWM = MeasureSpec.getMode(widthMeasureSpec);
        int specHS = MeasureSpec.getSize(heightMeasureSpec);
        int specHM = MeasureSpec.getMode(heightMeasureSpec);

        //todo solve the case when the specs would't be EXACTLY (currently, this will never happen)

        int w = resolveSizeAndState(measurePrefferedWidth(specHS), widthMeasureSpec, 1);
        int h = resolveSizeAndState(measureMinHeight(), heightMeasureSpec, 1);

        setMeasuredDimension(w, h);
    }

    public int measurePrefferedWidth(int height){
        double goldenRatio = (1 + Math.sqrt(5))/2d;

        double width = Math.max(measureMinWidth(), height / goldenRatio);
        return (int) width;
    }

    public int measureMinWidth() {
        int zkrprText = (int) (dividerWidth + paddingLeft + zkrprPaint.measureText("MATH") + paddingRight) + 1;
        int secondaryText = (int) (dividerWidth + paddingLeft + secondaryPaint.measureText("Tchr" + " ") + mistPaint.measureText("room 1") + paddingRight) + 1;
        return Math.max(Math.max(zkrprText, secondaryText), getSuggestedMinimumWidth());
    }

    /**
     * When the subject text is aligned to the center
     */
    public int measureMinPreferredHeight(){
        return ((zkrprTextHeight / 2) + textPadding + secondaryTextHeight) * 2 + dividerWidth + paddingTop + paddingBottom;
    }

    /**
     * When the texts are packed tightly together
     */
    public int measureMinHeight() {
        return dividerWidth + paddingTop + zkrprTextHeight + textPadding + secondaryTextHeight + paddingBottom;
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

        Paint paint;
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
        //noinspection SuspiciousNameCombination
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

            String zkrpr = hodina.getZkrpr();
            if (zkrpr == null || zkrpr.isEmpty())
                zkrpr = hodina.getZkratka();
            if (zkrpr == null)
                zkrpr = "";
            String zkrmist = hodina.getZkrmist();
            if (zkrmist == null)
                zkrmist = "";
            String zkruc = hodina.getZkruc();
            if (zkruc == null)
                zkruc = "";

            float actualSecondaryTextHeight = (zkrmist + zkruc).isEmpty() ? 0 : secondaryTextHeight;

            float zkrprBaseline = ((h - paddingTop - paddingBottom - dividerWidth) / 2f) + paddingTop + dividerWidth + (zkrprTextHeight / 2f);
            float middle = ((w - dividerWidth - paddingLeft - paddingRight) / 2f) + paddingLeft + dividerWidth;

            float secondaryBaseline = zkrprBaseline + textPadding + secondaryTextHeight;
            float secondaryTextWidth = secondaryPaint.measureText(zkruc + " " + zkrmist);
            float zkrucStart = middle - (secondaryTextWidth / 2f);
            float zkrmistStart = zkrucStart + secondaryPaint.measureText(zkruc + " ");

            if (h < (measureMinPreferredHeight() - (secondaryTextHeight - actualSecondaryTextHeight))){
                //do not align zkrpr to center (vertically)
                //secondary text will be aligned to the bottom and zkrpr to the center of the remaining space
                secondaryBaseline = h - paddingBottom;
                zkrprBaseline = (secondaryBaseline - secondaryTextHeight - dividerWidth - paddingTop) / 2 + dividerWidth + paddingTop + (zkrprTextHeight/2f);
                if (h < measureMinHeight()){
                }
            }

            // zkrpr
            // align is center
            canvas.drawText(zkrpr, middle, zkrprBaseline , zkrprPaint);

            //draw secondary = teacher and room
            mistPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(zkrmist, zkrmistStart, secondaryBaseline, mistPaint);

            secondaryPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(zkruc, zkrucStart, secondaryBaseline, secondaryPaint);

            /*// draw cycle
            if (perm && hodina.getCycle() != null && !hodina.getCycle().isEmpty()){
                float cycleBaseline = zkrprBaseline - zkrprTextHeight - textPadding;
                secondaryPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(hodina.getCycle(), middle, cycleBaseline, secondaryPaint);
            }*/
        }


    }

    private boolean addField(TableLayout layout, int resId, String fieldText){
        if (fieldText != null && !fieldText.trim().equals("")){
            TableRow tr = (TableRow) LayoutInflater.from(getContext()).inflate(R.layout.lesson_details_dialog_row,null);
            TextView tw1 = tr.findViewById(R.id.textViewKey);
            TextView tw2 = tr.findViewById(R.id.textViewValue);
            tw1.setText(getContext().getString(resId));
            tw2.setText(fieldText);
            //tw2.setMaxLines(8000);
            //tr.addView(tw1);
            //tr.addView(tw2,new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.addView(tr, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return true;
        }else {
            return false;
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

        TableLayout tableLayout = new TableLayout(getContext());
        tableLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        int density = (int) getContext().getResources().getDisplayMetrics().density;
        tableLayout.setPadding(24 * density, 16* density, 24 * density, 0);

        addField(tableLayout,R.string.notice, hodina.getNotice());
        if (perm){
            addField(tableLayout,R.string.cycle, hodina.getCycle());
        }
        addField(tableLayout,R.string.group, hodina.getSkup()); //you don't see group on the simplified tile anymore, therefore it is one of the main reasons you may want to see this dialog
        addField(tableLayout,R.string.lesson_teacher, hodina.getUc());
        if (!addField(tableLayout,R.string.room, hodina.getMist())){
            addField(tableLayout, R.string.room, hodina.getZkrmist());
        }

        addField(tableLayout,R.string.subject_name, hodina.getPr());
        addField(tableLayout,R.string.lesson_name, hodina.getNazev());
        addField(tableLayout,R.string.absence, hodina.getAbs());
        addField(tableLayout,R.string.topic, hodina.getTema());
        addField(tableLayout,R.string.change, hodina.getChng());

        builder.setView(tableLayout);
        builder.setPositiveButton(R.string.close, (dialog, which) -> {});

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

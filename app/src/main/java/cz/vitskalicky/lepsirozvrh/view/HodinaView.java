package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.OldRozvrhHodina;
import cz.vitskalicky.lepsirozvrh.theme.Theme;

public class HodinaView extends CellView {

    private OldRozvrhHodina hodina;
    private boolean perm;

    private Paint mistPaint;
    private Paint highlightPaint;
    private Paint highlightedDividerPaint;
    private Paint homeworkPaint;

    private int highlightWidth;
    private int homeworkSize;

    private boolean topHighlighted, leftHighlighted, cornerHighlighted;
    private boolean entireHighlighted; //the highlighting is thicker

    public HodinaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mistPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mistPaint.setColor(t.getCHRoomText());
        mistPaint.setTextSize(secondaryTextSize);
        mistPaint.setTypeface(Typeface.DEFAULT);
        mistPaint.setTextAlign(Paint.Align.LEFT);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(t.getCHighlight());
        highlightWidth = t.getPxHighlightWidth();
        highlightPaint.setStrokeWidth(highlightWidth);

        highlightedDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightedDividerPaint.setColor(t.getCHighlight());
        highlightedDividerPaint.setStrokeWidth(dividerWidth);

        homeworkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        homeworkPaint.setColor(t.getCHomework());

        homeworkSize = t.getPxHomework();

        setOnClickListener(v -> showDetailDialog());
        setDrawDividers(true, true, true);
    }

    @Override
    public int getMinimumWidth() {
        if (hodina != null) {
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
            if (((MainApplication)getContext().getApplicationContext()).getLogin().isTeacher()){
                // to teacher's we want to show the class, not the teacher
                // the class name is saved in zkrskup and skup
                zkruc = hodina.getZkrskup();
                if (zkruc == null || zkruc.isEmpty()){
                    zkruc = hodina.getSkup();
                }
                if (zkruc == null){
                    zkruc = "";
                }
            }
            int padding = super.getMinimumWidth();
            int primaryText = (int) primaryTextPaint.measureText(zkrpr) + 1;
            int secondaryText = (int) (secondaryTextPaint.measureText(zkruc + " ") + mistPaint.measureText(zkrmist)) + 1;
            return padding + Math.max(primaryText, secondaryText);
        } else {
            return super.getMinimumWidth();
        }
    }

    /**
     * Measures what the minimal width would be for an example cell with reasonably long texts.
     */
    public int measureExampleWidth() {
        int padding = super.getMinimumWidth();
        int primaryText = (int) primaryTextPaint.measureText("MATH") + 1;
        int secondaryText = (int) (secondaryTextPaint.measureText("Tchr" + " ") + mistPaint.measureText("VIII.B")) + 1;
        return padding + Math.max(primaryText, secondaryText);
    }

    /**
     * When the texts are packed tightly together
     */
    @Override
    public int getMinimumHeight() {
        return super.getMinimumHeight() + primaryTextSize + textPadding + secondaryTextSize;
    }

    /**
     * When the subject text is aligned to the center
     */
    public int getMinimalComfortableHeight() {
        return ((primaryTextSize / 2) + textPadding + secondaryTextSize) * 2 + super.getMinimumHeight();
    }

    /**
     * Updates the content
     */
    public void setHodina(OldRozvrhHodina hodina, boolean perm) {
        this.hodina = hodina;
        this.perm = perm;

        if (hodina == null) {
            backgroundPaint.setColor(t.getCEmptyBg());
            primaryTextPaint.setColor(t.getCHPrimaryText());
            secondaryTextPaint.setColor(t.getCHSecondaryText());
            mistPaint.setColor(t.getCHRoomText());
        } else if (hodina.getHighlight() == OldRozvrhHodina.CHANGED) {
            backgroundPaint.setColor(t.getCChngBg());
            primaryTextPaint.setColor(t.getCChngPrimaryText());
            secondaryTextPaint.setColor(t.getCChngSecondaryText());
            mistPaint.setColor(t.getCChngRoomText());
        } else if (hodina.getHighlight() == OldRozvrhHodina.NO_LESSON) {
            backgroundPaint.setColor(t.getCABg());
            primaryTextPaint.setColor(t.getCAPrimaryText());
            secondaryTextPaint.setColor(t.getCASecondaryText());
            mistPaint.setColor(t.getCARoomText());
        } else if (hodina.getHighlight() == OldRozvrhHodina.NONE) {
            backgroundPaint.setColor(t.getCHBg());
            primaryTextPaint.setColor(t.getCHPrimaryText());
            secondaryTextPaint.setColor(t.getCHSecondaryText());
            mistPaint.setColor(t.getCHRoomText());
        } else if (hodina.getHighlight() == OldRozvrhHodina.EMPTY) {
            backgroundPaint.setColor(t.getCEmptyBg());
            primaryTextPaint.setColor(t.getCHPrimaryText());
            secondaryTextPaint.setColor(t.getCHSecondaryText());
            mistPaint.setColor(t.getCHRoomText());
        }

        invalidate();
        requestLayout();
    }

    public OldRozvrhHodina getHodina() {
        return hodina;
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
        setDrawDividers(!topHighlighted, !cornerHighlighted, !leftHighlighted);
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        //# draw highlighted dividers
        //left
        if (leftHighlighted || entireHighlighted) {
            //noinspection SuspiciousNameCombination
            canvas.drawLine((float) dividerWidth / 2, dividerWidth, (float) dividerWidth / 2, h, highlightedDividerPaint);
        }

        //top
        if (topHighlighted || entireHighlighted) {
            canvas.drawLine(dividerWidth, (float) dividerWidth / 2, w, (float) dividerWidth / 2, highlightedDividerPaint);
        }

        //corner
        if (cornerHighlighted || entireHighlighted) {
            canvas.drawPoint(dividerWidth / 2f, dividerWidth / 2f, highlightedDividerPaint);
        }

        //highlight
        if (entireHighlighted) {
            canvas.drawLine(dividerWidth, dividerWidth + (highlightWidth / 2f), w, dividerWidth + (highlightWidth / 2f), highlightPaint);
            canvas.drawLine(w - (highlightWidth / 2f), dividerWidth + (highlightWidth / 2f), w - (highlightWidth / 2f), h - (highlightWidth / 2f), highlightPaint);
            canvas.drawLine(w, h - (highlightWidth / 2f), dividerWidth, h - (highlightWidth / 2f), highlightPaint);
            canvas.drawLine(dividerWidth + (highlightWidth / 2f), h - (highlightWidth / 2f), dividerWidth + (highlightWidth / 2f), dividerWidth + (highlightWidth / 2f), highlightPaint);
        }


    }

    @Override
    protected void onDrawContent(Canvas canvas, int xStart, int yStart, int xEnd, int yEnd) {
        int h = yEnd - yStart;
        int w = xEnd - xStart;

        //# draw texts
        if (hodina != null) {

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
            if (((MainApplication)getContext().getApplicationContext()).getLogin().isTeacher()){
                // to teacher's we want to show the class, not the teacher
                // the class name is saved in zkrskup and skup
                zkruc = hodina.getZkrskup();
                if (zkruc == null || zkruc.isEmpty()){
                    zkruc = hodina.getSkup();
                }
                if (zkruc == null){
                    zkruc = "";
                }
            }

            float actualSecondaryTextSize = (zkrmist + zkruc).isEmpty() ? 0 : secondaryTextSize;
            float actualPrimaryTextSize = primaryTextSize;

            if (canvas.getHeight() < getMinimumHeight()) {
                float overflow = (actualPrimaryTextSize + textPadding + actualSecondaryTextSize) - h;
                if (overflow < 0) {
                    overflow = 0;
                }
                actualPrimaryTextSize = actualPrimaryTextSize - overflow / ((actualPrimaryTextSize + actualSecondaryTextSize) / actualPrimaryTextSize);
                if (actualSecondaryTextSize > 0) {
                    actualSecondaryTextSize = actualSecondaryTextSize - overflow / ((primaryTextSize + actualSecondaryTextSize) / actualSecondaryTextSize);
                }
            }
            primaryTextPaint.setTextSize(actualPrimaryTextSize);
            secondaryTextPaint.setTextSize(actualSecondaryTextSize);
            mistPaint.setTextSize(actualSecondaryTextSize);

            float zkrprBaseline = (h / 2f) + (actualPrimaryTextSize / 2f);
            float middle = (w / 2f);

            float secondaryBaseline = zkrprBaseline + textPadding + actualSecondaryTextSize;
            float secondaryTextWidth = secondaryTextPaint.measureText(zkruc + " " + zkrmist);
            float zkrucStart = middle - (secondaryTextWidth / 2f);
            float zkrmistStart = zkrucStart + secondaryTextPaint.measureText(zkruc + " ");

            if (canvas.getHeight() < (getMinimalComfortableHeight() - (secondaryTextSize - actualSecondaryTextSize))) {
                //do not align zkrpr to center (vertically)
                //secondary text will be aligned to the bottom and zkrpr to the center of the remaining space
                secondaryBaseline = h;
                zkrprBaseline = (secondaryBaseline - actualSecondaryTextSize) / 2 + (actualPrimaryTextSize / 2f);
            }

            // zkrpr
            primaryTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(zkrpr, middle + xStart, zkrprBaseline + yStart, primaryTextPaint);

            //draw secondary = teacher and room
            mistPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(zkrmist, zkrmistStart + xStart, secondaryBaseline + yStart, mistPaint);

            secondaryTextPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(zkruc, zkrucStart + xStart, secondaryBaseline + yStart, secondaryTextPaint);

            //draw little dot if there is a homework
            if (!hodina.getUkolodevzdat().isEmpty()) {
                Paint use = homeworkPaint;
                if (!Theme.Utils.isLegible(homeworkPaint.getColor(), backgroundPaint.getColor(), 1.5)) {
                    use = primaryTextPaint;
                }
                canvas.drawCircle(xEnd - homeworkSize, yStart + homeworkSize, homeworkSize, use);
            }

            /*// draw cycle
            if (perm && hodina.getCycle() != null && !hodina.getCycle().isEmpty()){
                float cycleBaseline = zkrprBaseline - primaryTextSize - textPadding;
                secondaryTextPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(hodina.getCycle(), middle, cycleBaseline, secondaryTextPaint);
            }*/
        }
    }

    private boolean addField(TableLayout layout, int resId, String fieldText) {
        if (fieldText != null && !fieldText.isEmpty()) {
            TableRow tr = (TableRow) LayoutInflater.from(getContext()).inflate(R.layout.lesson_details_dialog_row, null);
            TextView tw1 = tr.findViewById(R.id.textViewKey);
            TextView tw2 = tr.findViewById(R.id.textViewValue);
            tw1.setText(getContext().getString(resId));
            tw2.setText(fieldText);
            //tw2.setMaxLines(8000);
            //tr.addView(tw1);
            //tr.addView(tw2,new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.addView(tr, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return true;
        } else {
            return false;
        }
    }

    public void showDetailDialog() {
        if (hodina == null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (hodina.getNazev() != null && !hodina.getNazev().trim().equals("")) {
            builder.setTitle(hodina.getNazev());
        } else if (hodina.getPr() != null && !hodina.getPr().trim().equals("")) {
            builder.setTitle(hodina.getPr());
        } else if (hodina.getZkrpr() != null && !hodina.getZkrpr().trim().equals("")) {
            builder.setTitle(hodina.getZkrpr());
        }

        TableLayout tableLayout = new TableLayout(getContext());
        tableLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        int density = (int) getContext().getResources().getDisplayMetrics().density;
        tableLayout.setPadding(24 * density, 16 * density, 24 * density, 0);

        addField(tableLayout, R.string.homework, hodina.getUkolodevzdat());
        addField(tableLayout, R.string.notice, hodina.getNotice());
        if (perm) {
            addField(tableLayout, R.string.cycle, hodina.getCycle());
        }
        addField(tableLayout, R.string.group, hodina.getSkup()); //you don't see group on the simplified tile anymore, therefore it is one of the main reasons you may want to see this dialog
        addField(tableLayout, R.string.lesson_teacher, hodina.getUc());
        if (!addField(tableLayout, R.string.room, hodina.getMist())) {
            addField(tableLayout, R.string.room, hodina.getZkrmist());
        }

        addField(tableLayout, R.string.subject_name, hodina.getPr());
        addField(tableLayout, R.string.lesson_name, hodina.getNazev());
        addField(tableLayout, R.string.absence, hodina.getAbs());
        addField(tableLayout, R.string.topic, hodina.getTema());
        addField(tableLayout, R.string.change, hodina.getChng());

        builder.setView(tableLayout);
        builder.setPositiveButton(R.string.close, (dialog, which) -> {
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

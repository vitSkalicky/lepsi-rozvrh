package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

/**
 * Class wrapping a {@code cell_hodina.xml} view.
 *
 * Every cell has a line (a.k.a. divider) on top and left (start) to create table dividers. When a cell is highlighted,
 * its right, bottom and corner neighbours change its line/corner color. The highlighted cell also
 * changes its line color, but also makes them thicker and show some 'thickness adding' lines on bottom
 * and right.
 */
public class HodinaCell {

    Context context;
    View view;
    RozvrhHodina hodina;
    boolean perm;
    float spread = 1;

    TextView twzkrpr;
    TextView twzkrmist;
    TextView twzkrskup;
    TextView twzkruc;
    TextView twzkruc2;
    TextView twcycle;

    View viewDividerTop; //divider
    View viewDividerLeft; //divider
    View viewDividerCorner; //divider
    View viewHighlighterTop; //adds thickness when highlighted
    View viewHighlighterRight; //adds thickness when highlighted
    View viewHighlighterBottom; //adds thickness when highlighted
    View viewHighlighterLeft; //adds thickness when highlighted

    public HodinaCell(Context context, RozvrhHodina hodina, ViewGroup parent, boolean perm) {
        this(context, parent);
        update(hodina, perm);
    }

    public HodinaCell(Context context, ViewGroup parent){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.cell_hodina, parent, false);

        twzkrpr = view.findViewById(R.id.textViewZkrpr);
        twzkrmist = view.findViewById(R.id.textViewZkrmist);
        twzkrskup = view.findViewById(R.id.textViewZkrskup);
        twzkruc = view.findViewById(R.id.textViewZkruc);
        twzkruc2 = view.findViewById(R.id.textViewZkruc2);
        twcycle = view.findViewById(R.id.textViewCycle);
        viewDividerTop = view.findViewById(R.id.viewDividerTop);
        viewDividerLeft = view.findViewById(R.id.viewDividerLeft);
        viewDividerCorner = view.findViewById(R.id.viewDividerCorner);
        viewHighlighterTop = view.findViewById(R.id.viewHighlighterTop);
        viewHighlighterRight = view.findViewById(R.id.viewHighlighterRight);
        viewHighlighterBottom = view.findViewById(R.id.viewHighlighterBottom);
        viewHighlighterLeft = view.findViewById(R.id.viewHighlighterLeft);

        twzkrpr.setText("");
        twzkrmist.setText("");
        twzkrskup.setText("");
        twzkruc.setText("");
        twzkruc2.setText("");
        twcycle.setText("");

        view.setOnClickListener(v -> {
            if (hodina == null)
                return;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
            builder.setPositiveButton(context.getString(R.string.close), (dialog, which) -> {});

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void addField(StringBuilder sb, int resId, String fieldText){
        if (fieldText != null && !fieldText.trim().equals("")){
            sb.append(context.getString(resId)).append(": ").append(fieldText).append("\n");
        }
    }

    public void update(RozvrhHodina hodina, boolean perm) {
        this.hodina = hodina;
        this.perm = perm;
        hightlightEdges(false, false, false);
        highlightItself(false);

        if (hodina != null) {
            twzkrpr.setText(hodina.getZkrpr());
            if (hodina.getZkrpr() == null || hodina.getZkrpr().equals(""))
                twzkrpr.setText(hodina.getZkratka());
            twzkrmist.setText(hodina.getZkrmist());
            twzkrskup.setText(hodina.getZkrskup());

            if (perm && hodina.getCycle() != null && !hodina.getCycle().equals("")){
                //show cycles on permanent schedule
                twzkruc.setText("");
                twzkruc2.setText(hodina.getZkruc());
                twcycle.setText(hodina.getCycle());
            }else {
                twzkruc.setText(hodina.getZkruc());
                twzkruc2.setText("");
                twcycle.setText("");
            }

            if (hodina.getHighlight() == RozvrhHodina.CHANGED) {
                view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhChang)));
            } else if (hodina.getHighlight() == RozvrhHodina.NO_LESSON) {
                view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhA)));
            } else if (hodina.getHighlight() == RozvrhHodina.NONE) {
                view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhH)));
            } else if (hodina.getHighlight() == RozvrhHodina.EMPTY) {
                view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhEmpty)));
            }
        }else {
            twzkrpr.setText("");
            twzkrmist.setText("");
            twzkrskup.setText("");
            twzkruc.setText("");
            twzkruc2.setText("");
            twcycle.setText("");
            view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhEmpty)));
        }
    }


    public void empty(){
        update(null,  false);
        updateSpread(1);
    }

    public View getView() {
        return view;
    }

    public void hightlightEdges(boolean top, boolean left, boolean corner){
        if (top)
            viewDividerTop.setBackgroundColor(ContextCompat.getColor(context, R.color.rozvrhDividerHighlight));
        else
            viewDividerTop.setBackgroundColor(ContextCompat.getColor(context, R.color.rozvrhDivider));

        if (left)
            viewDividerLeft.setBackgroundColor(ContextCompat.getColor(context, R.color.rozvrhDividerHighlight));
        else
            viewDividerLeft.setBackgroundColor(ContextCompat.getColor(context, R.color.rozvrhDivider));

        if (corner)
            viewDividerCorner.setBackgroundColor(ContextCompat.getColor(context, R.color.rozvrhDividerHighlight));
        else
            viewDividerCorner.setBackgroundColor(ContextCompat.getColor(context, R.color.rozvrhDivider));

    }

    public void highlightItself(boolean highlight){
        if (highlight){
            viewHighlighterTop.setVisibility(View.VISIBLE);
            viewHighlighterRight.setVisibility(View.VISIBLE);
            viewHighlighterBottom.setVisibility(View.VISIBLE);
            viewHighlighterLeft.setVisibility(View.VISIBLE);
        }else{
            viewHighlighterTop.setVisibility(View.GONE);
            viewHighlighterRight.setVisibility(View.GONE);
            viewHighlighterBottom.setVisibility(View.GONE);
            viewHighlighterLeft.setVisibility(View.GONE);
        }
    }

    public void updateSpread(float spread){
        this.spread = spread;
    }

}

package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class HodinaCell {

    Context context;
    CellView view;
    RozvrhHodina hodina;
    float weight;

    TextView twzkrpr;
    TextView twzkrmist;
    TextView twzkrskup;
    TextView twzkruc;

    public HodinaCell(Context context, RozvrhHodina hodina, float weight, ViewGroup parent, View top, int rows, int width) {
        this(context, parent, top, rows, width);
        update(hodina, weight);
    }

    public HodinaCell(Context context, ViewGroup parent, View top, int rows, int width){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (CellView) inflater.inflate(R.layout.cell_hodina, parent, false);
        view.init(top, rows, width);

        twzkrpr = view.findViewById(R.id.textViewZkrpr);
        twzkrmist = view.findViewById(R.id.textViewZkrmist);
        twzkrskup = view.findViewById(R.id.textViewZkrskup);
        twzkruc = view.findViewById(R.id.textViewZkruc);

        twzkrpr.setText("");
        twzkrmist.setText("");
        twzkrskup.setText("");
        twzkruc.setText("");

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

    public void update(RozvrhHodina hodina, float weight) {
        this.hodina = hodina;
        updateWeight(weight);

        if (hodina != null) {
            twzkrpr.setText(hodina.getZkrpr());
            if (hodina.getZkrpr() == null || hodina.getZkrpr().equals(""))
                twzkrpr.setText(hodina.getZkratka());
            twzkrmist.setText(hodina.getZkrmist());
            twzkrskup.setText(hodina.getZkrskup());
            twzkruc.setText(hodina.getZkruc());

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
            view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhEmpty)));
        }
    }

    public void updateWeight(float weight){
        this.weight = weight;

        view.setSpread(weight);
    }

    public void empty(){
        update(null, 1);
    }

    public View getView() {
        return view;
    }

}

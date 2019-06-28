package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
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
    }

    public void update(RozvrhHodina hodina, float weight) {
        this.hodina = hodina;
        updateWeight(weight);

        twzkrpr.setText(hodina.getZkrpr());
        if (hodina.getZkrpr() == null || hodina.getZkrpr().equals(""))
            twzkrpr.setText(hodina.getZkratka());
        twzkrmist.setText(hodina.getZkrmist());
        twzkrskup.setText(hodina.getZkrskup());
        twzkruc.setText(hodina.getZkruc());

        if (hodina.getHighlight() == RozvrhHodina.CHANGED){
            view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhX)));
        } else if (hodina.getHighlight() == RozvrhHodina.NO_LESSON){
            view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhA)));
        } else if (hodina.getHighlight() == RozvrhHodina.NONE){
            view.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.rozvrhH)));
        }
    }

    public void updateWeight(float weight){
        this.weight = weight;

        view.setSpread(weight);
    }

    public View getView() {
        return view;
    }

}

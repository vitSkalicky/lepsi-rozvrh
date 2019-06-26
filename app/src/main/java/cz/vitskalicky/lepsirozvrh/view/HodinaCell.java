package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class HodinaCell {

    Context context;
    ConstraintLayout view;
    RozvrhHodina hodina;
    float weight;

    TextView twzkrpr;
    TextView twzkrmist;
    TextView twzkrskup;
    TextView twzkruc;

    public HodinaCell(Context context, RozvrhHodina hodina, float weight, ViewGroup parent) {
        this(context, parent);
        update(hodina, weight);
    }

    public HodinaCell(Context context, ViewGroup parent){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (ConstraintLayout) inflater.inflate(R.layout.cell_hodina, parent, false);

        twzkrpr = view.findViewById(R.id.textViewZkrpr);
        twzkrmist = view.findViewById(R.id.textViewZkrmist);
        twzkrskup = view.findViewById(R.id.textViewZkrskup);
        twzkruc = view.findViewById(R.id.textViewZkruc);
    }

    public void update(RozvrhHodina hodina, float weight) {
        this.hodina = hodina;
        updateWeight(weight);

        twzkrpr.setText(hodina.getZkrpr());
        twzkrmist.setText(hodina.getZkrmist());
        twzkrskup.setText(hodina.getZkrskup());
        twzkruc.setText(hodina.getZkruc());
    }

    public void updateWeight(float weight){
        this.weight = weight;

        view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT,
                weight));
    }

    public View getView() {
        return view;
    }

}

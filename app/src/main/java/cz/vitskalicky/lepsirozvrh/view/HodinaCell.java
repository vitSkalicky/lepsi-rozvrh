package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class HodinaCell {

    Context context;
    ConstraintLayout view;
    RozvrhHodina hodina;

    TextView twzkrpr;
    TextView twzkrmist;
    TextView twzkrskup;
    TextView twzkruc;

    public HodinaCell(Context context, RozvrhHodina hodina, ViewGroup parent) {
        this(context, parent);
        update(hodina);
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

    public void update(RozvrhHodina hodina) {
        this.hodina = hodina;

        twzkrpr.setText(hodina.getZkrpr());
        twzkrmist.setText(hodina.getZkrmist());
        twzkrskup.setText(hodina.getZkrskup());
        twzkruc.setText(hodina.getZkruc());
    }

    public View getView() {
        return view;
    }

}

package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;

public class DenCell {

    Context context;
    ConstraintLayout view;
    RozvrhDen den;

    TextView twzkratka;
    TextView twdatum;

    public DenCell(Context context, RozvrhDen den, ViewGroup parent) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (ConstraintLayout) inflater.inflate(R.layout.cell_den, parent, false);

        twzkratka = view.findViewById(R.id.textViewZkratka);
        twdatum = view.findViewById(R.id.textViewDatum);

        update(den);

    }

    public void update(RozvrhDen den) {
        this.den = den;

        twzkratka.setText(den.getZkratka());
        twdatum.setText(den.getDatum());
    }

    public View getView() {
        return view;
    }
}

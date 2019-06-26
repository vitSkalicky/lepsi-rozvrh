package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;

public class DenCell {

    Context context;
    CellView view;
    RozvrhDen den;

    TextView twzkratka;
    TextView twdatum;

    public DenCell(Context context, RozvrhDen den, ViewGroup parent, View top, int rows) {
        this(context, parent, top, rows);
        update(den);
    }

    public DenCell(Context context, ViewGroup parent, View top, int rows){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (CellView) inflater.inflate(R.layout.cell_den, parent, false);
        view.init(top, rows);

        twzkratka = view.findViewById(R.id.textViewZkratka);
        twdatum = view.findViewById(R.id.textViewDatum);
    }

    public void update(RozvrhDen den) {
        this.den = den;

        twzkratka.setText(den.getZkratka());
        twdatum.setText(Utils.dateToLoacalizedString(Utils.parseDate(den.getDatum())));
    }

    public View getView() {
        return view;
    }
}

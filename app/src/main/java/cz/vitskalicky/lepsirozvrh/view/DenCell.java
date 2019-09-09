package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;

public class DenCell {

    Context context;
    CellView view;
    RozvrhDen den;

    TextView twzkratka;
    TextView twdatum;

    public DenCell(Context context, RozvrhDen den, ViewGroup parent, View top, int rows, int width) {
        this(context, parent, top, rows, width);
        update(den);
    }

    public DenCell(Context context, ViewGroup parent, View top, int rows, int width){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (CellView) inflater.inflate(R.layout.cell_den, parent, false);
        view.init(top, rows, width);

        twzkratka = view.findViewById(R.id.textViewZkratka);
        twdatum = view.findViewById(R.id.textViewDatum);
    }

    public void update(RozvrhDen den) {
        this.den = den;

        twzkratka.setText(den.getZkratka());
        LocalDate date = Utils.parseDate(den.getDatum());
        String dateText;
        if (den.getDatum() == null){
            dateText = "";
        }else{
            DateTimeFormatter dtf = DateTimeFormat.forPattern("d. M.");
            dateText = dtf.print(date);
        }

        twdatum.setText(dateText);
    }

    public void empty(){
        this.den = null;
        twdatum.setText("");
        twdatum.setText("");
    }

    public View getView() {
        return view;
    }
}

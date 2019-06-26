package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;

public class CornerCell {
    Context context;
    CellView view;

    String nazevcyklu;
    TextView twnazevcyklu;

    public CornerCell(Context context, Rozvrh rozvrh, ViewGroup parent, View top, int rows) {
        this(context, parent, top, rows);
        update(rozvrh);
    }

    public CornerCell(Context context, ViewGroup parent, View top, int rows){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (CellView) inflater.inflate(R.layout.cell_corner, parent, false);
        view.init(top, rows);

        twnazevcyklu = view.findViewById(R.id.textViewNazevcyklu);
    }

    public void update(Rozvrh rozvrh) {
        nazevcyklu = rozvrh.getNazevcyklu();
        twnazevcyklu.setText(nazevcyklu);
    }

    public View getView() {
        return view;
    }
}

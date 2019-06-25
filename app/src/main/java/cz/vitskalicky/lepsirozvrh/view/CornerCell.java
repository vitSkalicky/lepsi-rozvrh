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
    ConstraintLayout view;

    String nazevcyklu;
    TextView twnazevcyklu;

    public CornerCell(Context context, Rozvrh rozvrh, ViewGroup parent) {
        this(context, parent);
        update(rozvrh);
    }

    public CornerCell(Context context, ViewGroup parent){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (ConstraintLayout) inflater.inflate(R.layout.cell_hodina, parent, false);

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

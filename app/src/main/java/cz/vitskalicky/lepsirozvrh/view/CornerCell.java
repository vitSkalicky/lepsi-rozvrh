package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
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

    public CornerCell(Context context, Rozvrh rozvrh, ViewGroup parent, View top, int rows, int width) {
        this(context, parent, top, rows, width);
        update(rozvrh);
    }

    public CornerCell(Context context, ViewGroup parent, View top, int rows, int width){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (CellView) inflater.inflate(R.layout.cell_corner, parent, false);
        view.init(top, rows, width);

        twnazevcyklu = view.findViewById(R.id.textViewNazevcyklu);
    }

    public void update(Rozvrh rozvrh) {
        nazevcyklu = rozvrh.getNazevcyklu();
        twnazevcyklu.setText(nazevcyklu);
    }
    public void empty(){
        nazevcyklu = "";
        twnazevcyklu.setText("");
    }

    public View getView() {
        return view;
    }
}

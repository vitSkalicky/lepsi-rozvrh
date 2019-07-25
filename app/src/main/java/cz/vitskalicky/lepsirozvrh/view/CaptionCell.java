package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodinaCaption;

public class CaptionCell {
    Context context;
    CellView view;
    RozvrhHodinaCaption caption;

    TextView twcaption;
    TextView twbegin;
    TextView twend;

    public CaptionCell(Context context, RozvrhHodinaCaption caption, ViewGroup parent, View top, int rows, int width) {
        this(context, parent, top, rows, width);
        update(caption);

    }

    public CaptionCell(Context context, ViewGroup parent, View top, int rows, int width){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (CellView) inflater.inflate(R.layout.cell_caption, parent, false);
        view.init(top, rows, width);

        twcaption = view.findViewById(R.id.textViewCaption);
        twbegin = view.findViewById(R.id.textViewBegin);
        twend = view.findViewById(R.id.textViewEnd);
    }

    public void update(RozvrhHodinaCaption caption) {
        this.caption = caption;

        twcaption.setText(caption.getCaption());
        twbegin.setText(caption.getBegintime());
        twend.setText(caption.getEndtime());
    }
    public void empty(){
        caption = null;
        twcaption.setText("");
        twbegin.setText("");
        twend.setText("");
    }

    public View getView() {
        return view;
    }
}

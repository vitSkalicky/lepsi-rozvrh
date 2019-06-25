package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodinaCaption;

public class CaptionCell {
    Context context;
    ConstraintLayout view;
    RozvrhHodinaCaption caption;

    TextView twcaption;
    TextView twtime;

    public CaptionCell(Context context, RozvrhHodinaCaption caption, ViewGroup parent) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = (ConstraintLayout) inflater.inflate(R.layout.cell_caption, parent, false);

        twcaption = view.findViewById(R.id.textViewCaption);
        twtime = view.findViewById(R.id.textViewTime);

        update(caption);

    }

    public void update(RozvrhHodinaCaption caption) {
        this.caption = caption;

        twcaption.setText(caption.getCaption());
        twtime.setText(caption.getTimeString());
    }

    public View getView() {
        return view;
    }
}

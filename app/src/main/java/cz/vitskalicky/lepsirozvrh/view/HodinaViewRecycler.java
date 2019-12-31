package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

public class HodinaViewRecycler {
    private Context context;
    private List<HodinaView> buffer = new LinkedList<>();

    public HodinaViewRecycler(Context context) {
        this.context = context;
    }

    public void store(HodinaView... items){
        for (HodinaView item :items) {
            item.setHodina(null, false);
            buffer.add(item);
        }
    }

    public HodinaView retrieve(){
        if (buffer.size() > 0){
            return buffer.remove(0);
        }else {
            return new HodinaView(context, null);
        }
    }
}

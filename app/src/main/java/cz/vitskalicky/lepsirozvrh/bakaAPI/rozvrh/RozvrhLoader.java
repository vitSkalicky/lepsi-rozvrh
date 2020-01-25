package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh;

import android.content.Context;

import com.android.volley.RequestQueue;

import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RozvrhLoader {
    private Context context;
    private RequestQueue requestQueue;

    private HashMap<LocalDate, List<RozvrhRequest.Listener>> activeListeners = new HashMap<>();

    public RozvrhLoader(Context context, RequestQueue requestQueue) {
        this.context = context;
        this.requestQueue = requestQueue;
    }

    public void loadRozvrh(final LocalDate monday, RozvrhRequest.Listener listener){
        registerListener(monday, listener);
        RozvrhRequest request = new RozvrhRequest(monday, result -> {
            invokeListeners(monday, result);
        }, context);
        requestQueue.add(request);
    }

    private void registerListener(LocalDate monday ,RozvrhRequest.Listener listener){
        List<RozvrhRequest.Listener> list = activeListeners.get(monday);
        if (list == null){
            list = new LinkedList<>();
            activeListeners.put(monday, list);
        }
        list.add(listener);
    }

    private void invokeListeners(LocalDate monday, RozvrhRequest.Result result){
        List<RozvrhRequest.Listener> list = activeListeners.get(monday);
        if (list == null){
            return;
        }
        List<RozvrhRequest.Listener> copy = new LinkedList<>(list);
        for (RozvrhRequest.Listener item :copy) {
            item.method(result);
        }
        list.clear(); //Don't forget to delete them
    }
}

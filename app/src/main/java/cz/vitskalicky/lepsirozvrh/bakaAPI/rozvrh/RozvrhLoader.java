package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;

import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.Utils;

public class RozvrhLoader {
    public static final String TAG = RozvrhLoader.class.getSimpleName();
    private Context context;
    private RequestQueue requestQueue;

    private HashMap<LocalDate, List<RozvrhRequest.Listener>> activeListeners = new HashMap<>();
    private HashSet<LocalDate> requestInProcess = new HashSet<>();

    public RozvrhLoader(Context context, RequestQueue requestQueue) {
        this.context = context;
        this.requestQueue = requestQueue;
    }

    public void loadRozvrh(final LocalDate monday, RozvrhRequest.Listener listener){
        registerListener(monday, listener);
        if (!requestInProcess.contains(monday)){
            RozvrhRequest request = new RozvrhRequest(monday, result -> {
                invokeListeners(monday, result);
            }, context);
            requestQueue.add(request);
            requestInProcess.add(monday);
        }
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
        int failsafe = 0;
        while (list.size() > 0 && failsafe < 100){
            List<RozvrhRequest.Listener> copy = new LinkedList<>(list);
            list.clear(); //Don't forget to delete them
            for (RozvrhRequest.Listener item :copy) {
                item.method(result);
            }
            failsafe++;
            if (failsafe == 100){
                Log.e(TAG, "Possible infinite loop in requesting a rozvrh for week " + (monday == null ? "perm" : Utils.dateToString(monday)));
            }
        }
        requestInProcess.remove(monday);

    }
}

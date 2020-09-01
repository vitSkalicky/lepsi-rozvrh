package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3.Rozvrh3;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3.RozvrhConverter;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhRoot;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RozvrhLoader {
    public static final String TAG = RozvrhLoader.class.getSimpleName();
    private Context context;
    private RequestQueue requestQueue;

    private HashMap<LocalDate, List<Listener>> activeListeners = new HashMap<>();
    private HashSet<LocalDate> requestInProcess = new HashSet<>();

    public RozvrhLoader(Context context, RequestQueue requestQueue) {
        this.context = context;
        this.requestQueue = requestQueue;
    }

    public void loadRozvrh(final LocalDate monday, Listener listener) {
        loadRozvrh(monday, listener, false);
    }

    public void loadRozvrh(final LocalDate monday, Listener listener, boolean isRetry){
        if (!isRetry){
            registerListener(monday, listener);
        }
        if (!requestInProcess.contains(monday) || isRetry){

            Retrofit retrofit = ((MainApplication)context.getApplicationContext()).getRetrofit();
            if (retrofit == null){
                Result result = new Result();
                result.code = ResponseCode.LOGIN_FAILED;
                result.raw = "no url";
                result.rozvrh = null;
                invokeListeners(monday, result);
                return;
            }

            RozvrhAPIInterface apiInterface = retrofit.create(RozvrhAPIInterface.class);

            boolean perm = monday == null;
            Call<Rozvrh3> call;

            if (perm){
                call = apiInterface.getPermanentSchedule();
            }else {
                call = apiInterface.getActualSchedule(monday.toString("YYYY-MM-dd"));
            }

            call.enqueue(new Callback<Rozvrh3>() {
                @Override
                public void onResponse(Call<Rozvrh3> call, Response<Rozvrh3> response) {
                    if (response.isSuccessful()){
                        Result result = new Result();
                        result.code = ResponseCode.SUCCESS;
                        try {
                            result.raw = new ObjectMapper().writeValueAsString(response.body());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        RozvrhRoot root = RozvrhConverter.convert(response.body(),perm, context);
                        root.checkDemoMode(context);
                        result.rozvrh = root.getRozvrh();
                        invokeListeners(monday, result);
                    }else {
                        if (response.code() == 401 /*Unauthorized*/){
                            if (!isRetry) {
                                //refresh token and try again
                                ((MainApplication) context.getApplicationContext()).getLogin().refreshToken(code -> {
                                    loadRozvrh(monday, listener, true);
                                });
                            }else {
                                Result result = new Result();
                                result.code = ResponseCode.LOGIN_FAILED;
                                try {
                                    result.raw = response.errorBody().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                result.rozvrh = null;
                                invokeListeners(monday, result);
                            }
                        }else {
                            Result result = new Result();
                            result.code = ResponseCode.UNEXPECTED_RESPONSE;
                            try {
                                result.raw = response.errorBody().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            result.rozvrh = null;
                            invokeListeners(monday, result);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Rozvrh3> call, Throwable t) {
                    Result result = new Result();
                    result.code = ResponseCode.UNREACHABLE;
                    result.raw = t.getMessage();
                    result.rozvrh = null;
                    t.printStackTrace();
                    invokeListeners(monday, result);
                }
            });
            if (!isRetry) {
                requestInProcess.add(monday);
            }
        }
    }

    private void registerListener(LocalDate monday , Listener listener){
        List<Listener> list = activeListeners.get(monday);
        if (list == null){
            list = new LinkedList<>();
            activeListeners.put(monday, list);
        }
        list.add(listener);
    }

    private void invokeListeners(LocalDate monday, Result result){
        List<Listener> list = activeListeners.get(monday);
        if (list == null){
            return;
        }
        int failsafe = 0;
        while (list.size() > 0 && failsafe < 100){
            List<Listener> copy = new LinkedList<>(list);
            list.clear(); //Don't forget to delete them
            for (Listener item :copy) {
                item.method(result);
            }
            failsafe++;
            if (failsafe == 100){
                Log.e(TAG, "Possible infinite loop in requesting a rozvrh for week " + (monday == null ? "perm" : Utils.dateToString(monday)));
            }
        }
        requestInProcess.remove(monday);

    }

    public static interface Listener {
        public void method(Result result);
    }

    public static class Result {
        /**
         * {@code null} when error
         */
        Rozvrh rozvrh;
        /**
         * Raw rozvrh xml when success, raw response when error
         */
        String raw;
        /**
         * One of the {@link ResponseCode} codes;
         */
        int code;

        public Result(Rozvrh rozvrh, String raw, int code) {
            this.rozvrh = rozvrh;
            this.raw = raw;
            this.code = code;
        }

        public Result() {
        }
    }
}

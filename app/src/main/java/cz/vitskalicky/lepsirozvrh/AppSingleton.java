package cz.vitskalicky.lepsirozvrh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.vitskalicky.lepsirozvrh.widget.WidgetsSettings;

public class AppSingleton {
    private static final String TAG = AppSingleton.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static AppSingleton instance;
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;
    //private RequestQueue requestQueue;
    //private RozvrhAPI rozvrhAPI;
    private WidgetsSettings widgetsSettings;

    private AppSingleton(Context context) {
        ctx = context;
        //requestQueue = getRequestQueue();
    }

    public static synchronized AppSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new AppSingleton(context.getApplicationContext());
        }
        return instance;
    }

    /*public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx);
        }
        return requestQueue;
    }

    public RozvrhAPI getRozvrhAPI() {
        if (rozvrhAPI == null) {
            rozvrhAPI = new RozvrhAPI(getRequestQueue(), ctx.getApplicationContext());
        }
        return rozvrhAPI;
    }*/

    /**
     * Update these widget settings and don't forget to {@link #saveWidgetsSettings()} afterwards.
     */
    public WidgetsSettings getWidgetsSettings() {
        if (widgetsSettings == null) {
            if (SharedPrefs.contains(ctx, SharedPrefs.WIDGETS_SETTINGS)) {
                ObjectMapper mapper = new ObjectMapper();

                try {
                    widgetsSettings = mapper.readValue(SharedPrefs.getString(ctx, SharedPrefs.WIDGETS_SETTINGS), WidgetsSettings.class);
                } catch (JsonProcessingException e) {
                    widgetsSettings = new WidgetsSettings();
                }
            } else {
                widgetsSettings = new WidgetsSettings();
            }
        }
        return widgetsSettings;

    }

    public void saveWidgetsSettings() {
        if (widgetsSettings != null){
            ObjectMapper mapper = new ObjectMapper();

            try {
                String json = mapper.writeValueAsString(widgetsSettings);
                SharedPrefs.setString(ctx, SharedPrefs.WIDGETS_SETTINGS, json);
            } catch (JsonProcessingException e) {
                Log.e(TAG, "Failed to save widgets settings (widgets count: " + widgetsSettings.widgetIds.size() + ")");
            }
        }
    }
}


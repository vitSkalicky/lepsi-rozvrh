package cz.vitskalicky.lepsirozvrh.bakaAPI;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.InvalidFormatException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Target;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.vitskalicky.lepsirozvrh.MainActivity;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhRoot;

public class RozvrhAPI {
    private static String TAG = RozvrhAPI.class.getCanonicalName();
    public static interface ResponseListener {
        public void onResponse(int code, String response);
    }

    public static interface RozvrhListener {
        public void onResponse(int code, Rozvrh rozvrh);
    }

    public static final int SUCCESS = 0;
    public static final int LOGIN_FAILED = 1;
    public static final int UNEXPECTED_RESPONSE = 2;
    public static final int UNREACHABLE = 3;
    public static final int NO_CACHE = 4;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");


    /**
     * Gets raw xml document from the server.
     * @param mondayDate Date of monday of the requested week. If {@code null}, permanent timetable is returned.
     * @param listener ResponseListener for returning data
     */
    private static void fetchXml(Calendar mondayDate, ResponseListener listener, RequestQueue requestQueue, Context context){
        String strDate;
        if (mondayDate == null){
            strDate = "perm";
        }else {
            strDate = dateFormat.format(mondayDate.getTime());
        }

        String url = SharedPrefs.getString(context, SharedPrefs.URL);
        String token = Login.getToken(context);
        String fullUrl = url + "?hx=" + token + "&pm=rozvrh&pmd=" + strDate;

        StringRequest request = new StringRequest(Request.Method.GET, fullUrl, response -> {
            int retCode;
            String retResponse;
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(new ByteArrayInputStream(response.getBytes()));

                Element root = document.getDocumentElement();
                root.normalize();

                int result = Integer.parseInt(root.getElementsByTagName("result").item(0).getTextContent());

                if (result == -1){// Login incorrect
                    Log.i(TAG,"Getting timetable failed: login incorrect: url: " + url + " Date: " + strDate + " response:\n" + response);
                    retCode = LOGIN_FAILED;
                    retResponse = response;
                }else {
                    retCode = SUCCESS;
                    retResponse = response;
                }
            } catch (ParserConfigurationException | IOException | SAXException | NullPointerException | NumberFormatException e) {
                Log.e(TAG, "Getting timetable failed: unexpected response: url: " + url + " Date: " + strDate + " error message: " + e.getMessage() + " response:\n" + response);
                e.printStackTrace();
                listener.onResponse(UNEXPECTED_RESPONSE, response);
                return;
            }
            listener.onResponse(retCode, retResponse);
        },error -> {
            Log.i(TAG,"Getting timetable failed: network error: " + error.getMessage());
            listener.onResponse(UNREACHABLE, "");
            return;
        });
        requestQueue.add(request);
    }

    private static void fetchRozvrh(Calendar mondayDate, RozvrhListener listener, RequestQueue requestQueue, Context context){
        fetchXml(mondayDate, (code, response) -> {
            if (code == SUCCESS){
                RozvrhRoot root;
                try{
                    Serializer serializer = new Persister();
                    root = serializer.read(RozvrhRoot.class, response);
                } catch (Exception e) {
                    Log.e(TAG, "Timetable deserialization failed. error message: " + e.getMessage() + " raw xml:\n" + response);
                    e.printStackTrace();
                    listener.onResponse(UNEXPECTED_RESPONSE, null);
                    return;
                }
                listener.onResponse(SUCCESS, root.getRozvrh());
            }else{
                listener.onResponse(code, null);
            }
        }, requestQueue, context);
    }

    /**
     * Saved timetable for later faster loading. Saving is performed on background thread and file
     * writing is thread-safe.
     * @param monday monday for week identification, leave null for permanent timetable
     * @param rozvrh string containing timetable xml
     */
    private static void saveRawRozvrh(Calendar monday, String rozvrh, Context context){
        AsyncTask.execute(() -> {
            Calendar sureMonday = null;
            if (monday != null)
                sureMonday = Utils.getWeekMonday(monday); //just to be extra sure

            String filename;
            if (sureMonday == null){
                filename = "rozvrh-perm.xml";
            }else{
                filename = "rozvrh-" + Utils.dateToString(sureMonday) + ".xml";
            }


            try (FileOutputStream outputStream = context.openFileOutput(filename,Context.MODE_PRIVATE);
                 FileLock lock = outputStream.getChannel().lock() ){

                outputStream.write(rozvrh.getBytes());

            } catch (Exception e) {
                Log.e(TAG, "Timetable saving failed: error message: " + e.getMessage() + " stack trace:");
                e.printStackTrace();
            }
        });
    }

    private static void loadRozvrh(Calendar monday, RozvrhListener listener, Context context){
        AsyncTask.execute(() -> {
            Calendar sureMonday = null;
            if (monday != null)
                sureMonday = Utils.getWeekMonday(monday); //just to be extra sure

            String filename;
            if (sureMonday == null){
                filename = "rozvrh-perm.xml";
            }else{
                filename = "rozvrh-" + Utils.dateToString(sureMonday) + ".xml";
            }

            RozvrhRoot root;
            try (FileInputStream inputStream = context.openFileInput(filename);
                 FileLock lock = inputStream.getChannel().lock() ){

                Serializer serializer = new Persister();
                root = serializer.read(RozvrhRoot.class, inputStream);


            } catch (FileNotFoundException e) {
                if (sureMonday != null)
                    System.out.println("Timetable for week " + Utils.dateToString(sureMonday) + " not found.");
                else
                    System.out.println("Timetable for week " + "perm" + " not found.");

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.onResponse(NO_CACHE, null));
                return;
            } catch (Exception e){
                Log.e(TAG, "Timetable loading failed: error message: " + e.getMessage() + " stack trace:");
                e.printStackTrace();

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.onResponse(NO_CACHE, null));
                return;
            }
            new Handler(Looper.getMainLooper()).post(() ->
                    listener.onResponse(SUCCESS, root.getRozvrh()));
        });
    }

    /**
     * Loads timetable for given week from cache (if there is none, code {@link #NO_CACHE} is returned),
     * which is returned using {@code onCacheLoaded} listener. Meanwhile timetable is fetched from server
     * and returned using {@code onLoaded} listener.
     *
     * @param mondayDate Date of monday of requested week or {@code null} for permanent timetable
     * @param requestQueue Request queue to be used for network requests
     * @param onCacheLoaded Listener using which cached timetable is returned
     * @param onLoaded Listener using which fetched timetable is returned
     */
    public static void getRozvrh(Calendar mondayDate, RequestQueue requestQueue, Context context, RozvrhListener onCacheLoaded, RozvrhListener onLoaded){
        fetchXml(mondayDate,(code, xmlString) -> {
            if (code == SUCCESS){
                Rozvrh ret;
                try{
                    Serializer serializer = new Persister();
                    RozvrhRoot root = serializer.read(RozvrhRoot.class, xmlString);
                    ret = root.getRozvrh();
                } catch (Exception e) {
                    Log.e(TAG, "Timetable deserialization failed. error message: " + e.getMessage() + " raw xml:\n" + xmlString);
                    e.printStackTrace();
                    onLoaded.onResponse(UNEXPECTED_RESPONSE, null);
                    return;
                }
                saveRawRozvrh(mondayDate, xmlString, context);
                onLoaded.onResponse(SUCCESS, ret);
            }else{
                onLoaded.onResponse(code, null);
                return;
            }
        }, requestQueue, context);

        loadRozvrh(mondayDate,onCacheLoaded, context);
    }


}

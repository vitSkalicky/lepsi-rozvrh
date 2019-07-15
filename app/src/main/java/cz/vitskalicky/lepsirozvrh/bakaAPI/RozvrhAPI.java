package cz.vitskalicky.lepsirozvrh.bakaAPI;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.joda.time.LocalDate;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhRoot;

public class RozvrhAPI {
    public static final int SUCCESS = 0;
    public static final int LOGIN_FAILED = 1;
    public static final int UNEXPECTED_RESPONSE = 2;
    public static final int UNREACHABLE = 3;
    public static final int NO_CACHE = 4;

    private static String TAG = RozvrhAPI.class.getSimpleName();

    /**
     * Gets raw xml document from the server.
     *
     * @param mondayDate Date of monday of the requested week. If {@code null}, permanent timetable is returned.
     * @param listener   ResponseListener for returning data
     */
    private static void fetchXml(LocalDate mondayDate, ResponseListener listener, RequestQueue requestQueue, Context context) {
        String strDate;
        if (mondayDate == null) {
            strDate = "perm";
        } else {
            strDate = Utils.dateToString(mondayDate);
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

                if (result == -1) {// Login incorrect
                    Log.i(TAG, "Getting timetable failed: login incorrect: url: " + url + " Date: " + strDate + " response:\n" + response);
                    retCode = LOGIN_FAILED;
                    retResponse = response;
                } else {
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
        }, error -> {
            Log.i(TAG, "Getting timetable failed: network error: " + error.getMessage());
            listener.onResponse(UNREACHABLE, "");
            return;
        });
        requestQueue.add(request);
    }

    private static RozvrhRoot parseRozvrh(String string){
        RozvrhRoot root;
        try {
            Serializer serializer = new Persister();
            root = serializer.read(RozvrhRoot.class, string);
        } catch (Exception e) {
            Log.e(TAG, "Timetable deserialization failed. error message: " + e.getMessage() + " raw xml:\n" + string);
            e.printStackTrace();
            return null;
        }
        return root;
    }

    private static void fetchRozvrh(LocalDate mondayDate, RozvrhListener listener, RequestQueue requestQueue, Context context) {
        fetchXml(mondayDate, (code, response) -> {
            if (code == SUCCESS) {
                RozvrhRoot root = parseRozvrh(response);

                if (root == null || root.getRozvrh() == null){
                    listener.method(UNEXPECTED_RESPONSE, null);
                    return;
                }
                listener.method(SUCCESS, root.getRozvrh());
            } else {
                listener.method(code, null);
            }
        }, requestQueue, context);
    }

    /**
     * Saved timetable for later faster loading. Saving is performed on background thread and file
     * writing is thread-safe.
     *
     * @param monday monday for week identification, leave null for permanent timetable
     * @param rozvrh string containing timetable xml
     */
    private static void saveRawRozvrh(LocalDate monday, String rozvrh, Context context) {
        AsyncTask.execute(() -> {
            LocalDate sureMonday = null;
            if (monday != null)
                sureMonday = Utils.getWeekMonday(monday); //just to be extra sure

            String filename;
            if (sureMonday == null) {
                filename = "rozvrh-perm.xml";
            } else {
                filename = "rozvrh-" + Utils.dateToString(sureMonday) + ".xml";
            }


            try (FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                 FileLock lock = outputStream.getChannel().lock()) {

                outputStream.write(rozvrh.getBytes());

            } catch (Exception e) {
                Log.e(TAG, "Timetable saving failed: error message: " + e.getMessage() + " stack trace:");
                e.printStackTrace();
            }
        });
    }

    private static void loadRozvrh(LocalDate monday, RozvrhListener listener, Context context) {
        AsyncTask.execute(() -> {
            LocalDate sureMonday = null;
            if (monday != null)
                sureMonday = Utils.getWeekMonday(monday); //just to be extra sure

            String filename;
            if (sureMonday == null) {
                filename = "rozvrh-perm.xml";
            } else {
                filename = "rozvrh-" + Utils.dateToString(sureMonday) + ".xml";
            }

            RozvrhRoot root;
            try (FileInputStream inputStream = context.openFileInput(filename)) {

                Serializer serializer = new Persister();
                root = serializer.read(RozvrhRoot.class, inputStream);


            } catch (FileNotFoundException e) {
                if (sureMonday != null)
                    System.out.println("Timetable for week " + Utils.dateToString(sureMonday) + " not found.");
                else
                    System.out.println("Timetable for week " + "perm" + " not found.");

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.method(NO_CACHE, null));
                return;
            } catch (Exception e) {
                Log.e(TAG, "Timetable loading failed: error message: " + e.getMessage() + " stack trace:");
                e.printStackTrace();

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.method(NO_CACHE, null));
                return;
            }
            new Handler(Looper.getMainLooper()).post(() ->
                    listener.method(SUCCESS, root.getRozvrh()));
        });
    }

    /**
     * Deletes Rozvrhs saved in 'cache' which are older than month. operation are run on background.
     */
    public static void deleteOldCache(Context context){
        AsyncTask.execute(() -> {

            File dir = context.getFilesDir();

            LocalDate deleteBefore = LocalDate.now().minusMonths(1);

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File fileDir, String name) {
                    if (fileDir == dir && name.length() > 11){
                        String date = name.substring(7,name.length() - 4);

                        if (date.equals("perm")) return false;

                        LocalDate fileDate;
                        try {
                            fileDate = Utils.parseDate(date);
                        }catch (IllegalArgumentException e){
                            return false;
                        }

                        if (fileDate.isBefore(deleteBefore)){
                            return true;
                        }else {
                            return false;
                        }
                    }
                    return false;
                }
            };

            String[] fileNames = dir.list(filter);

            for (String item :fileNames) {
                context.deleteFile(item);
            }
        });
    }

    /**
     * Loads timetable for given week from cache (if there is none, code {@link #NO_CACHE} is returned),
     * which is returned using {@code onCacheLoaded} listener. Meanwhile timetable is fetched from server
     * and returned using {@code onLoaded} listener.
     *
     * @param mondayDate    Date of monday of requested week or {@code null} for permanent timetable
     * @param requestQueue  Request queue to be used for network requests
     * @param onCacheLoaded Listener using which cached timetable is returned
     * @param onLoaded      Listener using which fetched timetable is returned
     */
    public static void getRozvrh(LocalDate mondayDate, RequestQueue requestQueue, Context context, RozvrhListener onCacheLoaded, RozvrhListener onLoaded) {
        fetchXml(mondayDate, (code, xmlString) -> {
            if (code == SUCCESS) {
                Rozvrh ret;
                try {
                    Serializer serializer = new Persister();
                    RozvrhRoot root = serializer.read(RozvrhRoot.class, xmlString);
                    ret = root.getRozvrh();
                } catch (Exception e) {
                    Log.e(TAG, "Timetable deserialization failed. error message: " + e.getMessage() + " raw xml:\n" + xmlString);
                    e.printStackTrace();
                    onLoaded.method(UNEXPECTED_RESPONSE, null);
                    return;
                }
                saveRawRozvrh(mondayDate, xmlString, context);
                onLoaded.method(SUCCESS, ret);
            } else {
                onLoaded.method(code, null);
                return;
            }
        }, requestQueue, context);

        loadRozvrh(mondayDate, onCacheLoaded, context);
    }

    public static interface ResponseListener {
        public void onResponse(int code, String response);
    }

    /**
     * Listener for returning {@link Rozvrh} objects after slow operation (network of file access)
     */
    public static interface RozvrhListener {
        /**
         * The only method
         * @param code status code identifying success or failure; is one of those:
         *             {@link #SUCCESS}, {@link #LOGIN_FAILED}, {@link #UNEXPECTED_RESPONSE}, {@link #UNREACHABLE}, {@link #NO_CACHE},
         * @param rozvrh
         */
        public void method(int code, Rozvrh rozvrh);
    }



    public static int getRememberedRows(Context context){
        if(!SharedPrefs.contains(context, SharedPrefs.REMEMBERED_ROWS))
            return 0;
        return SharedPrefs.getInt(context, SharedPrefs.REMEMBERED_ROWS);
    }

    public static int getRememberedColumns(Context context){
        if(!SharedPrefs.contains(context, SharedPrefs.REMEMBERED_COLUMNS))
            return 0;
        return SharedPrefs.getInt(context, SharedPrefs.REMEMBERED_COLUMNS);
    }

    public static void rememberRows(Context context, int rows){
        SharedPrefs.setInt(context, SharedPrefs.REMEMBERED_ROWS, rows);
    }

    public static void rememberColumns(Context context, int columns){
        SharedPrefs.setInt(context, SharedPrefs.REMEMBERED_COLUMNS, columns);
    }

    // NOT-STATIC PART OF THIS CLASS
    // =============================
    // This class should be created by an Activity to manage loaded Rozvrh object.

    private HashMap<LocalDate, Rozvrh> saved = new HashMap<>();
    private RequestQueue requestQueue;
    private Context context;

    public RozvrhAPI(RequestQueue requestQueue, Context context) {
        this.requestQueue = requestQueue;
        this.context = context;
    }

    /**
     * Gets Rozvrh from:
     *  - Memory (this objects's private field) - only Rozvrhs requested on this object ore available there - instant
     *  - File storage ('cache') - only Rozvrhs requested on this device are available - under 1 second
     *  - Network (school Bakaláři server) - only available if connected to internet - slow
     * Neither file storage nor network will be used if requested rozvrh is found in memory.
     * It is not guaranteed that <code>onCacheLoaded</code> will be called before <code>onNetLoaded</code>!
     * @param date Monday date identifying week or <code>null</code> for permanent timetable.
     * @param onCacheLoaded returns Rozvrh object loaded from 'cache'. Will not be called if rozvrh was found in memory.
     *                      {@code code} = {@link #SUCCESS} -> loading successful, object is in {@code rozvrh}.
     *                      {@code code} = {@link #NO_CACHE} -> Not found in cache or error while loading. {@code rozvrh} is <code>null</code>.
     * @param onNetLoaded returns Rozvrh object fetched from server. Will not be called if rozvrh was found in memory.
     *                    {@code code} = {@link #SUCCESS} -> Loading successful, object is in <code>rozvrh</code>.
     *                    {@code code} = {@link #LOGIN_FAILED} -> Logging in failed (user's password has changed?). <code>rozvrh</code> is <code>null</code>.
     *                    {@code code} = {@link #UNEXPECTED_RESPONSE} -> Unexpected response from server (bad login? API has changed? Rozvrh module is not supported?). <code>rozvrh</code> is <code>null</code>.
     *                    {@code code} = {@link #UNREACHABLE} -> Server unreachable or other network error (no connection probably).  <code>rozvrh</code> is <code>null</code>.
     * @return Rozvrh object loaded from memory or null if Rozvrh is not in memory
     */
    public Rozvrh get(LocalDate date, RozvrhListener onCacheLoaded, RozvrhListener onNetLoaded){
        final LocalDate monaday = Utils.getWeekMonday(date); //just to be extra sure
        Rozvrh memory = saved.get(monaday);

        if (memory == null) {
            loadRozvrh(monaday, (code, rozvrh) -> {
                if (code == SUCCESS && saved.get(monaday) == null) {
                    saved.put(monaday, rozvrh);
                }
                onCacheLoaded.method(code, rozvrh);
            }, context);

            fetchXml(monaday, (code, response) -> {
                if (code == SUCCESS) {
                    RozvrhRoot root = parseRozvrh(response);
                    if (root == null || root.getRozvrh() == null) {
                        onCacheLoaded.method(UNEXPECTED_RESPONSE, null);
                        return;
                    }

                    saved.put(monaday, root.getRozvrh());
                    saveRawRozvrh(monaday, response, context);

                    onNetLoaded.method(SUCCESS, root.getRozvrh());
                    return;
                }
                onNetLoaded.method(code, null);
            }, requestQueue, context);
        }

        return memory;
    }

    /**
     * Clears object's Rozvrh storage - all rozvrhs will have to load from cache and server again.
     */
    public void refresh(){
        saved.clear();
    }
}

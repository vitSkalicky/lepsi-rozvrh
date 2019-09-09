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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhRoot;

/**
 * This class is responsible for fetching, parsing and caching schedule (CZ: rozvrh).
 *
 * All downloaded schedules are immediately cached (a.k.a. File storage). This cached date is then loaded
 * when internet connection is not available and also while loading the 'live' data to show schedule
 * to user as soon as possible.
 *
 * When data is loaded from cache or network, it is stored in a private field in object's memory
 * (a.k.a. Memory). From now on, when showing these data again (user switched to different week and
 * then returns) loading is instant.
 *
 * Cache data older than month should be deleted every time the app exits. This has to be handled
 * by an activity or something else by calling {@link #clearOldCache(Context)} on exit.
 */
public class RozvrhAPI {
    public static final int SUCCESS = 0;
    public static final int LOGIN_FAILED = 1;
    public static final int UNEXPECTED_RESPONSE = 2;
    public static final int UNREACHABLE = 3;
    public static final int NO_CACHE = 4;

    private static String TAG = RozvrhAPI.class.getSimpleName();
    public static final String TAG_TIMER = TAG + "-timer";

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

    /**
     * Parses rozvrh xml given as string argument
     * @param string rozvrh xml
     * @return parsed rozvrh or null if failed
     */
    private static RozvrhRoot parseRozvrh(String string) {
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

    /**
     * Same as as calling {@link #fetchXml(LocalDate, ResponseListener, RequestQueue, Context)} and {@link #parseRozvrh(String)}
     */
    private static void fetchRozvrh(LocalDate mondayDate, RozvrhListener listener, RequestQueue requestQueue, Context context) {
        fetchXml(mondayDate, (code, response) -> {
            if (code == SUCCESS) {
                RozvrhRoot root = parseRozvrh(response);

                if (root == null || root.getRozvrh() == null) {
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
     * Saved rozvrh for later faster loading. Saving is performed on background thread and file
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

    /**
     * Loads Rozvrh from cache on background thread.
     *
     * @param monday   Monday identifying week or null for permanent
     * @param listener listener for returnening data. Codes:
     *                 {@link #NO_CACHE} - Not found in cache - {@code rozvrh) is null
     *                 {@link #SUCCESS} - Loading succeeded - requested rozvrh is in {@code rozvrh)
     */
    private static void loadRozvrh(LocalDate monday, RozvrhListener listener, Context context) {
        //debug timing: Log.d(TAG_TIMER, "file load main start " + Utils.getDebugTime());
        new Thread(() -> {
            //debug timing: Log.d(TAG_TIMER, "file load async start " + Utils.getDebugTime());
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
            //debug timing: Log.d(TAG_TIMER, "file load async end " + Utils.getDebugTime());
        }).start();

        //debug timing: Log.d(TAG_TIMER, "file load main end " + Utils.getDebugTime());
    }

    /**
     * Deletes Rozvrhs saved in 'cache' which are older than month. Operations are run on background.
     * Should be called on every app exit (or just time by time).
     */
    public static void clearOldCache(Context context) {
        AsyncTask.execute(() -> {

            File dir = context.getFilesDir();

            LocalDate deleteBefore = LocalDate.now().minusMonths(1);

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File fileDir, String name) {
                    if (fileDir == dir && name.length() > 11) {
                        String date = name.substring(7, name.length() - 4);

                        if (date.equals("perm")) return false;

                        LocalDate fileDate;
                        try {
                            fileDate = Utils.parseDate(date);
                        } catch (IllegalArgumentException e) {
                            return false;
                        }

                        if (fileDate.isBefore(deleteBefore)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                }
            };

            String[] fileNames = dir.list(filter);

            for (String item : fileNames) {
                context.deleteFile(item);
            }
        });
    }

    /**
     * Deletes all rovrhs save din cache. Operations are run on background.
     */
    public static void clearCache(Context context) {
        AsyncTask.execute(() -> {

            File dir = context.getFilesDir();

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File fileDir, String name) {
                    if (fileDir == dir) {
                        if (name.equals("rozvrh-perm.xml")) return true;
                        else return name.matches("rozvrh-[0-9]{8}\\.xml");
                    }
                    return false;
                }
            };

            String[] fileNames = dir.list(filter);

            for (String item : fileNames) {
                context.deleteFile(item);
            }
        });
    }

    /**
     * Loads rozvrh for given week from cache (if there is none, code {@link #NO_CACHE} is returned),
     * which is returned using {@code onCacheLoaded} listener. Meanwhile rozvrh is fetched from server
     * and returned using {@code onLoaded} listener. {@code onLoaded} might be called before
     * {@code onCacheLoaded}, especially when network is not available.
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
         *
         * @param code   status code identifying success or failure; is one of those:
         *               {@link #SUCCESS}, {@link #LOGIN_FAILED}, {@link #UNEXPECTED_RESPONSE}, {@link #UNREACHABLE}, {@link #NO_CACHE},
         * @param rozvrh
         */
        public void method(int code, Rozvrh rozvrh);
    }


    public static int getRememberedRows(Context context) {
        if (!SharedPrefs.contains(context, SharedPrefs.REMEMBERED_ROWS))
            return 0;
        return SharedPrefs.getInt(context, SharedPrefs.REMEMBERED_ROWS);
    }

    public static int getRememberedColumns(Context context) {
        if (!SharedPrefs.contains(context, SharedPrefs.REMEMBERED_COLUMNS))
            return 0;
        return SharedPrefs.getInt(context, SharedPrefs.REMEMBERED_COLUMNS);
    }

    public static void rememberRows(Context context, int rows) {
        SharedPrefs.setInt(context, SharedPrefs.REMEMBERED_ROWS, rows);
    }

    public static void rememberColumns(Context context, int columns) {
        SharedPrefs.setInt(context, SharedPrefs.REMEMBERED_COLUMNS, columns);
    }

    // NOT-STATIC PART OF THIS CLASS
    // =============================
    // This class should be created by an Activity to manage loaded Rozvrh object.

    private HashMap<LocalDate, Rozvrh> saved = new HashMap<>();
    private RequestQueue requestQueue;
    private Context context;
    private Map<LocalDate, RozvrhListener> activeListeners = new HashMap<>();
    private Set<LocalDate> active = new HashSet<>();

    public RozvrhAPI(RequestQueue requestQueue, Context context) {
        this.requestQueue = requestQueue;
        this.context = context;
    }

    /**
     * Gets Rozvrh from:
     * - Memory (this objects's private field) - only Rozvrhs requested on this object ore available there - instant
     * - File storage ('cache') - only Rozvrhs requested on this device are available - under 1 second
     * - Network (school Bakaláři server) - only available if connected to internet - slow
     * Network won't be used, but cache (file storage) might, if requested rozvrh is found in memory.
     * It is not guaranteed that <code>onCacheLoaded</code> will be called before <code>onNetLoaded</code>!
     *
     * @param date          Monday date identifying week or <code>null</code> for permanent timetable.
     * @param onCacheLoaded returns Rozvrh object loaded from 'cache'. Will not be called if rozvrh was found in memory.
     *                      {@code code} = {@link #SUCCESS} -> loading successful, object is in {@code rozvrh}.
     *                      {@code code} = {@link #NO_CACHE} -> Not found in cache or error while loading. {@code rozvrh} is <code>null</code>.
     * @param onNetLoaded   returns Rozvrh object fetched from server. Will not be called if rozvrh was found in memory.
     *                      {@code code} = {@link #SUCCESS} -> Loading successful, object is in <code>rozvrh</code>.
     *                      {@code code} = {@link #LOGIN_FAILED} -> Logging in failed (user's password has changed?). <code>rozvrh</code> is <code>null</code>.
     *                      {@code code} = {@link #UNEXPECTED_RESPONSE} -> Unexpected response from server (bad login? API has changed? Rozvrh module is not supported?). <code>rozvrh</code> is <code>null</code>.
     *                      {@code code} = {@link #UNREACHABLE} -> Server unreachable or other network error (no connection probably).  <code>rozvrh</code> is <code>null</code>.
     * @return Rozvrh object loaded from memory or null if Rozvrh is not in memory
     */
    public Rozvrh get(LocalDate date, RozvrhListener onCacheLoaded, RozvrhListener onNetLoaded) {
        final LocalDate monday = Utils.getWeekMonday(date); //just to be extra sure
        Rozvrh ret = null;
        if (!active.contains(monday)) {
            ret = getOne(monday, onCacheLoaded, onNetLoaded);
        } else {
            ret = null;
            Rozvrh memory = saved.get(monday);
            if (memory == null) {
                loadRozvrh(monday, (code, rozvrh) -> {
                    if (code == SUCCESS && saved.get(monday) == null) {
                        saved.put(monday, rozvrh);
                    }
                    onCacheLoaded.method(code, rozvrh);
                }, context);
            } else {
                ret = memory;
            }
            activeListeners.put(monday, onNetLoaded);
        }

        //cache permanent, current
        cacheCNPP();

        //cache next and prev
        cacheNP(monday);

        return ret;
    }

    /**
     * Caches week to memory (and file cache) and fetches a fresh one from network. (Only if it is not in memory already)
     *
     * @param date monday identifying week.
     */
    public void cacheWeek(LocalDate date) {
        final LocalDate monday = Utils.getWeekMonday(date); //just to be extra sure
        if (!saved.containsKey(monday)) {
            fetchXml(monday, (code, response) -> {
                RozvrhListener listener = activeListeners.get(monday);

                if (code == SUCCESS) {
                    RozvrhRoot root = parseRozvrh(response);
                    if (root == null || root.getRozvrh() == null) {
                        if (listener != null)
                            listener.method(UNEXPECTED_RESPONSE, null);
                        return;
                    }

                    saved.put(monday, root.getRozvrh());
                    saveRawRozvrh(monday, response, context);

                    if (listener != null)
                        listener.method(SUCCESS, root.getRozvrh());
                    return;
                }
                if (listener != null)
                    listener.method(code, null);

                activeListeners.remove(monday);
                active.remove(monday);

            }, requestQueue, context);
            active.add(monday);
        }
    }

    /**
     * Cache Current, Next, Previous, Permanent.
     */
    private void cacheCNPP() {
        cacheWeek(null);
        cacheWeek(Utils.getCurrentMonday());
        cacheWeek(Utils.getCurrentMonday().plusWeeks(1));
        cacheWeek(Utils.getCurrentMonday().minusWeeks(1));
    }

    /**
     * Cache next abd previous relative to given week.
     */
    private void cacheNP(LocalDate date) {
        final LocalDate monday = Utils.getWeekMonday(date); //just to be extra sure
        if (monday == null)
            return;
        LocalDate nextMonday = monday.plusWeeks(1);
        cacheWeek(nextMonday);

        LocalDate prevMonday = monday.minusWeeks(1);
        cacheWeek(prevMonday);
    }

    /**
     * Gets Rozvrh from:
     * - Memory (this objects's private field) - only Rozvrhs requested on this object ore available there - instant
     * - File storage ('cache') - only Rozvrhs requested on this device are available - under 1 second
     * - Network (school Bakaláři server) - only available if connected to internet - slow
     * Neither file storage nor network will be used if requested rozvrh is found in memory.
     * It is not guaranteed that <code>onCacheLoaded</code> will be called before <code>onNetLoaded</code>!
     * <b>Use {@link #get(LocalDate, RozvrhListener, RozvrhListener)} because it caches Rozvrhs in a smarter way.</b>
     *
     * @param date          Monday date identifying week or <code>null</code> for permanent timetable.
     * @param onCacheLoaded returns Rozvrh object loaded from 'cache'. Will not be called if rozvrh was found in memory.
     *                      {@code code} = {@link #SUCCESS} -> loading successful, object is in {@code rozvrh}.
     *                      {@code code} = {@link #NO_CACHE} -> Not found in cache or error while loading. {@code rozvrh} is <code>null</code>.
     * @param onNetLoaded   returns Rozvrh object fetched from server. Will not be called if rozvrh was found in memory.
     *                      {@code code} = {@link #SUCCESS} -> Loading successful, object is in <code>rozvrh</code>.
     *                      {@code code} = {@link #LOGIN_FAILED} -> Logging in failed (user's password has changed?). <code>rozvrh</code> is <code>null</code>.
     *                      {@code code} = {@link #UNEXPECTED_RESPONSE} -> Unexpected response from server (bad login? API has changed? Rozvrh module is not supported?). <code>rozvrh</code> is <code>null</code>.
     *                      {@code code} = {@link #UNREACHABLE} -> Server unreachable or other network error (no connection probably).  <code>rozvrh</code> is <code>null</code>.
     * @return Rozvrh object loaded from memory or null if Rozvrh is not in memory
     */
    private Rozvrh getOne(LocalDate date, RozvrhListener onCacheLoaded, RozvrhListener onNetLoaded) {
        final LocalDate monday = Utils.getWeekMonday(date); //just to be extra sure
        Rozvrh memory = saved.get(monday);

        if (memory == null) {
            loadRozvrh(monday, (code, rozvrh) -> {
                if (code == SUCCESS && saved.get(monday) == null) {
                    saved.put(monday, rozvrh);
                }
                onCacheLoaded.method(code, rozvrh);
            }, context);

            fetchXml(monday, new ResponseListener() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == SUCCESS) {
                        RozvrhRoot root = parseRozvrh(response);
                        if (root == null || root.getRozvrh() == null) {
                            onNetLoaded.method(UNEXPECTED_RESPONSE, null);
                            return;
                        }

                        saved.put(monday, root.getRozvrh());
                        saveRawRozvrh(monday, response, context);

                        onNetLoaded.method(SUCCESS, root.getRozvrh());
                        return;
                    }
                    if (code == LOGIN_FAILED)
                    onNetLoaded.method(code, null);
                }
            }, requestQueue, context);
        }

        return memory;
    }

    /**
     * Clears object's Rozvrh storage - all rozvrhs will have to load from cache and server again.
     */
    public void clearMemory() {
        saved.clear();
    }

    /**
     * Clears memory and requests new timetable from net, if connection fails, it loads it from cache,
     * if it doesn't it clears cache and saves the new one to cache and returns it using {@code onLoaded}
     * listener. Codes:
     * - {@link #SUCCESS} - successfully fetched new timetable from server and cleared cache. Refreshed timetable is in {@code rozvrh}.
     * - {@link #UNREACHABLE} - could not get response from server, loaded timetable from cache and cache was not cleared. {@code rozvrh} is the one loaded from cache or {@code null} if there was none in cache.
     * - {@link #UNEXPECTED_RESPONSE} - error in parsing fetched data, loaded timetable from cache and cache was not cleared. {@code rozvrh} is the one loaded from cache or {@code null} if there was none in cache.
     * - {@link #LOGIN_FAILED} - Response contaned message indicating faile login, loaded timetable from cache and cache was not cleared. {@code rozvrh} is the one loaded from cache or {@code null} if there was none in cache.
     *
     * @param monday monday identifying week or {@code null} for permanent timetable.
     */
    public void refresh(LocalDate monday, RozvrhListener onLoaded) {
        clearMemory();
        fetchXml(monday, (code, xmlString) -> {
            if (code == SUCCESS) {
                Rozvrh ret;
                try {
                    Serializer serializer = new Persister();
                    RozvrhRoot root = serializer.read(RozvrhRoot.class, xmlString);
                    ret = root.getRozvrh();
                } catch (Exception e) {
                    Log.e(TAG, "Timetable deserialization failed. error message: " + e.getMessage() + " raw xml:\n" + xmlString);
                    e.printStackTrace();
                    completeRefresh(monday, null, UNEXPECTED_RESPONSE, onLoaded);
                    return;
                }
                clearCache(context);
                saveRawRozvrh(monday, xmlString, context);
                completeRefresh(monday, ret, code, onLoaded);
                cacheCNPP();
                cacheNP(monday);
            } else {
                completeRefresh(monday, null, code, onLoaded);
                return;
            }
        }, requestQueue, context);
    }

    /**
     * Helper method for {@link #refresh(LocalDate, RozvrhListener)}
     *
     * @param rozvrh if null, fetching from net failed
     */
    private void completeRefresh(LocalDate monday, Rozvrh rozvrh, int netResponseCode, RozvrhListener onLoaded) {
        if (rozvrh == null) {
            //no net
            loadRozvrh(monday, (code, rozvrh1) -> {
                if (code == SUCCESS) {
                    onLoaded.method(netResponseCode, rozvrh1);
                } else {
                    onLoaded.method(netResponseCode, null);
                }
            }, context);
        } else {
            onLoaded.method(SUCCESS, rozvrh);
        }
    }
}

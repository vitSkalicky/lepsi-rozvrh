package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhRoot;

import static cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode.*;

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
    private static String TAG = RozvrhAPI.class.getSimpleName();
    public static final String TAG_TIMER = TAG + "-timer";

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
     *                 {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#NO_CACHE} - Not found in cache - {@code rozvrh) is null
     *                 {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#SUCCESS} - Loading succeeded - requested rozvrh is in {@code rozvrh)
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
     * Loads rozvrh for given week from cache (if there is none, code {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#NO_CACHE} is returned),
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
        RozvrhRequest request = new RozvrhRequest(mondayDate, successResult -> {
            saveRawRozvrh(mondayDate, successResult.raw, context);
            onLoaded.method(SUCCESS, successResult.rozvrh);
        }, errorResult -> {
            onLoaded.method(errorResult.code, null);
        }, context);
        requestQueue.add(request);

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
         *               {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#SUCCESS}, {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#LOGIN_FAILED}, {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#UNEXPECTED_RESPONSE}, {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#UNREACHABLE}, {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#NO_CACHE},
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
    private Map<LocalDate, List<RozvrhListener>> activeListeners = new HashMap<>(); //listeners for each week
    private Set<LocalDate> active = new HashSet<>(); //which weeks have active listeners attached to them
    private Map<LocalDate, LocalTime> lastUpdated = new HashMap<>();

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
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#SUCCESS} -> loading successful, object is in {@code rozvrh}.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#NO_CACHE} -> Not found in cache or error while loading. {@code rozvrh} is <code>null</code>.
     * @param onNetLoaded   returns Rozvrh object fetched from server. Will not be called if rozvrh was found in memory.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#SUCCESS} -> Loading successful, object is in <code>rozvrh</code>.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#LOGIN_FAILED} -> Logging in failed (user's password has changed?). <code>rozvrh</code> is <code>null</code>.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#UNEXPECTED_RESPONSE} -> Unexpected response from server (bad login? API has changed? Rozvrh module is not supported?). <code>rozvrh</code> is <code>null</code>.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#UNREACHABLE} -> Server unreachable or other network error (no connection probably).  <code>rozvrh</code> is <code>null</code>.
     * @return Rozvrh object loaded from memory or null if Rozvrh is not in memory
     */
    public Rozvrh get(LocalDate date, RozvrhListener onCacheLoaded, RozvrhListener onNetLoaded) {
        final LocalDate monday = Utils.getWeekMonday(date); //just to be extra sure
        Rozvrh ret = null;
        if (!active.contains(monday)) {
            ret = getOne(monday, onCacheLoaded, onNetLoaded);
        } else {
            Rozvrh memory = getFromMemory(monday);
            if (memory == null) {
                loadRozvrh(monday, (code, rozvrh) -> {
                    if (code == SUCCESS && getFromMemory(monday) == null) {
                        putToMemory(monday, rozvrh);
                    }
                    onCacheLoaded.method(code, rozvrh);
                }, context);
            } else {
                ret = memory;
            }
            addWeekLoadListener(monday, onNetLoaded);
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
        if (!isInMemory(monday) && !active.contains(monday)) {
            RozvrhRequest request = new RozvrhRequest(monday, result -> {
                if (result.code == SUCCESS) {
                    putToMemory(monday, result.rozvrh);
                    saveRawRozvrh(monday, result.raw, context);

                    triggerWeekLoadListeners(monday, result.code, result.rozvrh);
                    return;
                }
                triggerWeekLoadListeners(monday, result.code, null);
            }, context);

            requestQueue.add(request);

            active.add(monday);
        }
    }

    /**
     * Cache Current, Next, Previous, Permanent.
     */
    private void cacheCNPP() {
        LocalDate monday = Utils.getDisplayWeekMonday(context);
        cacheWeek(null);
        cacheWeek(monday);
        cacheWeek(monday.plusWeeks(1));
        cacheWeek(monday.minusWeeks(1));
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
     * Adds listener to active listeners ({@link #activeListeners}) and marks week as active
     * ({@link #active}). Linsteners in {@link #activeListeners} will be triggered when the week
     * finishes loading.
     */
    private void addWeekLoadListener(LocalDate week, RozvrhListener onNetLoaded){
        List<RozvrhListener> list = activeListeners.get(week);
        if (list == null){
            list = new LinkedList<>();
            activeListeners.put(week, list);
        }
        list.add(onNetLoaded);
        active.add(week);
    }

    private void triggerWeekLoadListeners(LocalDate week, int code, Rozvrh rozvrh){
        List<RozvrhListener> list = activeListeners.get(week);
        if (list != null){
            for (RozvrhListener item :list) {
                item.method(code, rozvrh);
            }
            list.clear();
        }
        active.remove(week);
    }



    /**
     * Prevents rozvrhs from being in memory for too long and therefore forces them to be refreshed from net after 3 hours
     */
    private Rozvrh putToMemory(LocalDate date, Rozvrh item){
        lastUpdated.put(date, LocalTime.now());
        return saved.put(date, item);
    }

    /**
     * Prevents rozvrhs from being in memory for too long and therefore forces them to be refreshed from net after 3 hours
     */
    private Rozvrh getFromMemory(LocalDate date){
        LocalTime updateTime = lastUpdated.get(date);
        if (updateTime == null || updateTime.isAfter(LocalTime.now().minusHours(3))){
            return saved.get(date);
        }else {
            lastUpdated.remove(date);
            saved.remove(date);
            return null;
        }
    }

    private boolean isInMemory(LocalDate date){
        LocalTime updateTime = lastUpdated.get(date);
        if (updateTime == null || updateTime.isAfter(LocalTime.now().minusHours(3))){
            return saved.containsKey(date);
        }else {
            lastUpdated.remove(date);
            saved.remove(date);
            return false;
        }
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
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#SUCCESS} -> loading successful, object is in {@code rozvrh}.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#NO_CACHE} -> Not found in cache or error while loading. {@code rozvrh} is <code>null</code>.
     * @param onNetLoaded   returns Rozvrh object fetched from server. Will not be called if rozvrh was found in memory.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#SUCCESS} -> Loading successful, object is in <code>rozvrh</code>.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#LOGIN_FAILED} -> Logging in failed (user's password has changed?). <code>rozvrh</code> is <code>null</code>.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#UNEXPECTED_RESPONSE} -> Unexpected response from server (bad login? API has changed? Rozvrh module is not supported?). <code>rozvrh</code> is <code>null</code>.
     *                      {@code code} = {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#UNREACHABLE} -> Server unreachable or other network error (no connection probably).  <code>rozvrh</code> is <code>null</code>.
     * @return Rozvrh object loaded from memory or null if Rozvrh is not in memory
     */
    private Rozvrh getOne(LocalDate date, RozvrhListener onCacheLoaded, RozvrhListener onNetLoaded) {
        final LocalDate monday = Utils.getWeekMonday(date); //just to be extra sure
        Rozvrh memory = getFromMemory(monday);

        if (memory == null) {
            loadRozvrh(monday, (code, rozvrh) -> {
                if (code == SUCCESS && getFromMemory(monday) == null) {
                    putToMemory(monday, rozvrh);
                }
                onCacheLoaded.method(code, rozvrh);
            }, context);

            addWeekLoadListener(monday, onNetLoaded);

            RozvrhRequest request = new RozvrhRequest(monday, successResult -> {
                putToMemory(monday, successResult.rozvrh);
                saveRawRozvrh(monday, successResult.raw, context);

                triggerWeekLoadListeners(monday,SUCCESS, successResult.rozvrh);
            }, errorResult -> {
                triggerWeekLoadListeners(monday,errorResult.code, null);
            }, context);

            requestQueue.add(request);
        }

        return memory;
    }

    /**
     * Clears object's Rozvrh storage - all rozvrhs will have to load from cache and server again.
     */
    public void clearMemory() {
        lastUpdated.clear();
        saved.clear();
    }

    /**
     * Clears memory and requests new timetable from net, if connection fails, it loads it from cache,
     * if it doesn't it clears cache and saves the new one to cache and returns it using {@code onLoaded}
     * listener. Codes:
     * - {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#SUCCESS} - successfully fetched new timetable from server and cleared cache. Refreshed timetable is in {@code rozvrh}.
     * - {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#UNREACHABLE} - could not get response from server, loaded timetable from cache and cache was not cleared. {@code rozvrh} is the one loaded from cache or {@code null} if there was none in cache.
     * - {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#UNEXPECTED_RESPONSE} - error in parsing fetched data, loaded timetable from cache and cache was not cleared. {@code rozvrh} is the one loaded from cache or {@code null} if there was none in cache.
     * - {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#LOGIN_FAILED} - Response contaned message indicating faile login, loaded timetable from cache and cache was not cleared. {@code rozvrh} is the one loaded from cache or {@code null} if there was none in cache.
     *
     * @param monday monday identifying week or {@code null} for permanent timetable.
     */
    public void refresh(LocalDate monday, RozvrhListener onLoaded) {
        clearMemory();
        RozvrhRequest request = new RozvrhRequest(monday, result -> {
            if (result.code == SUCCESS) {
                clearCache(context);
                saveRawRozvrh(monday, result.raw, context);
                putToMemory(monday, result.rozvrh);
                cacheCNPP();
                cacheNP(monday);

                onLoaded.method(SUCCESS, result.rozvrh);
            } else {
                loadRozvrh(monday, (code, rozvrh1) -> {
                    if (code == SUCCESS) {
                        onLoaded.method(result.code, rozvrh1);
                        putToMemory(monday,rozvrh1);
                    } else {
                        onLoaded.method(result.code, null);
                    }
                }, context);
            }
        }, context);
        requestQueue.add(request);
    }
}

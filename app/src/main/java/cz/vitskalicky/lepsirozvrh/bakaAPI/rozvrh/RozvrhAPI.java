package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.RequestQueue;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vitskalicky.lepsirozvrh.Mutable;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;

import static cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode.NO_CACHE;
import static cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode.SUCCESS;

/**
 * This class is responsible for fetching, parsing and caching schedule (CZ: rozvrh).
 * <p>
 * All downloaded schedules are immediately cached (a.k.a. File storage). This cached date is then loaded
 * when internet connection is not available and also while loading the 'live' data to show schedule
 * to user as soon as possible.
 * <p>
 * When data is loaded from cache or network, it is stored in a private field in object's memory
 * (a.k.a. Memory). From now on, when showing these data again (user switched to different week and
 * then returns) loading is instant.
 * <p>
 * Cache data older than month should be deleted every time the app exits. This has to be handled
 * by an activity or something else by calling {@link RozvrhCache#clearOldCache(Context)} on exit.
 */
public class RozvrhAPI {
    private static String TAG = RozvrhAPI.class.getSimpleName();
    public static final String TAG_TIMER = TAG + "-timer";


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

    private HashMap<LocalDate, Rozvrh> saved = new HashMap<>();
    private RequestQueue requestQueue;
    private Context context;
    private Map<LocalDate, LocalTime> lastUpdated = new HashMap<>();
    private HashMap<LocalDate, List<RozvrhListener>> listeners = new HashMap<>();
    private HashMap<LocalDate, MutableLiveData<RozvrhWrapper>> liveDatas = new HashMap<>();
    private RozvrhLoader rozvrhLoader;

    public RozvrhAPI(RequestQueue requestQueue, Context context) {
        this.requestQueue = requestQueue;
        this.context = context;
        rozvrhLoader = new RozvrhLoader(context, requestQueue);
    }

    /**
     * Returns {@link LiveData} for the selected week. THey will be updated with the most recent data.
     * Also, if the rozvrh is in memory, the LiveData will be updated right away.
     *
     * <b>NOTE: source = {@link RozvrhWrapper#SOURCE_NET}, rozvrh != null, but the
     * code != {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode#SUCCESS} means that rozvrh
     * could not be fetched from server, so the attached one is from cache.</b>
     *
     * @param monday Monday identifying week.
     */
    public LiveData<RozvrhWrapper> getLiveData(LocalDate monday) {
        MutableLiveData<RozvrhWrapper> ld = liveDatas.get(monday);
        if (ld == null) {
            ld = new MutableLiveData<RozvrhWrapper>();
            liveDatas.put(monday, ld);
        }

        final MutableLiveData<RozvrhWrapper> fld = ld;

        //update live data
        Rozvrh rozvrh = getFromMemory(monday);
        // if the rozvrh is already in memory, don't fetch anything from net
        if (rozvrh == null) {
            final Mutable<Boolean> netFinishedSucessfully = new Mutable<>(false);
            getFromCacheAndSave(monday, rozvrhWrapper -> {
                if (!netFinishedSucessfully.getValue()) {
                    fld.setValue(rozvrhWrapper);
                }
            });
            getFromNetAndSave(monday, rozvrhWrapper -> {
                if (rozvrhWrapper.getCode() == SUCCESS) {
                    fld.setValue(rozvrhWrapper);
                    netFinishedSucessfully.setValue(true);
                } else {
                    Rozvrh prevRozvrh = fld.getValue() == null ? null : fld.getValue().getRozvrh();
                    fld.setValue(new RozvrhWrapper(prevRozvrh, rozvrhWrapper.getCode(), RozvrhWrapper.SOURCE_NET));
                }
            });
            fld.setValue(new RozvrhWrapper(null, NO_CACHE, RozvrhWrapper.SOURCE_MEMORY));
        } else {
            fld.setValue(new RozvrhWrapper(rozvrh, SUCCESS, RozvrhWrapper.SOURCE_MEMORY));
        }

        cacheCNPP();
        cacheNP(monday);
        return fld;
    }

    private void getFromCacheAndSave(LocalDate monday, RozvrhListener listener) {
        RozvrhCache.loadRozvrh(monday, rozvrhWrapper -> {
            // cache is always 'righter' than memory
            putToMemory(monday, rozvrhWrapper.getRozvrh());
            updateLiveData(monday, rozvrhWrapper);
            listener.method(rozvrhWrapper);
        }, context);
    }

    /**
     * saves the fetched rozvrh to cache and memory
     */
    private void getFromNetAndSave(LocalDate monday, RozvrhListener listener) {
        rozvrhLoader.loadRozvrh(monday, result -> {
            RozvrhWrapper rw = new RozvrhWrapper(result.rozvrh, result.code, RozvrhWrapper.SOURCE_NET);
            if (result.code == SUCCESS) {
                RozvrhCache.saveRawRozvrh(monday, result.raw, context);
                putToMemory(monday, result.rozvrh);
                updateLiveData(monday, rw);
            }
            listener.method(rw);
        });
    }

    private void updateLiveData(LocalDate monday, RozvrhWrapper rw) {
        MutableLiveData<RozvrhWrapper> ld = liveDatas.get(monday);
        if (ld != null) {
            ld.setValue(rw);
        }
    }

    /**
     * Simply get the rozvrh and calls the listener whet it has the best result. onFinishedListener may even
     * be called immediately if requested rozvrh is in memory.
     */
    public void getRozvrh(LocalDate monday, RozvrhListener listener) {
        Mutable<Boolean> theOther = new Mutable<>(false);
        Mutable<Integer> netCode = new Mutable<>(-1);
        Mutable<RozvrhWrapper> cacheResult = new Mutable<>(null);
        //just to be sure
        Mutable<Boolean> wasInMemory = new Mutable<>(false);
        Rozvrh rozvrh = getFromMemory(monday);
        if (rozvrh == null) {
            getFromCacheAndSave(monday, rozvrhWrapper -> {
                if (wasInMemory.getValue()) {
                    return;
                }
                if (netCode.getValue() == SUCCESS) {
                    return;
                } else if (theOther.getValue()) {
                    listener.method(rozvrhWrapper);
                } else {
                    cacheResult.setValue(rozvrhWrapper);
                }
                theOther.setValue(true);
            });
            getFromNetAndSave(monday, rozvrhWrapper -> {
                if (wasInMemory.getValue()) {
                    return;
                }
                netCode.setValue(rozvrhWrapper.getCode());
                if (rozvrhWrapper.getCode() == SUCCESS) {
                    listener.method(rozvrhWrapper);
                } else if (cacheResult.getValue() != null && cacheResult.getValue().getCode() == SUCCESS) {
                    listener.method(cacheResult.getValue());
                } else if (theOther.getValue()) {
                    listener.method(new RozvrhWrapper(null, rozvrhWrapper.getCode(), RozvrhWrapper.SOURCE_NET));
                }
                theOther.setValue(true);
            });
        }
        wasInMemory.setValue(rozvrh != null);
        if (rozvrh != null) {
            listener.method(new RozvrhWrapper(rozvrh, SUCCESS, RozvrhWrapper.SOURCE_MEMORY));
        }
    }

    /**
     * Fetches a fresh rozvrh from net and saves it to cache and memory for future use. (Only if it is not in memory already)
     *
     * @param date monday identifying week.
     */
    public void cacheWeek(LocalDate date) {
        final LocalDate monday = Utils.getWeekMonday(date); //just to be extra sure
        if (!isInMemory(monday)) {
            getFromNetAndSave(monday, rozvrhWrapper -> {
            });
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
     * Cache next and previous relatively to the given week.
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
     * Puts a rozvrh into object's memory. Also prevents rozvrhs from being in memory for too long and therefore forces them to be refreshed from net after 3 hours
     */
    private Rozvrh putToMemory(LocalDate date, Rozvrh item) {
        lastUpdated.put(date, LocalTime.now());
        return saved.put(date, item);
    }

    /**
     * Prevents rozvrhs from being in memory for too long and therefore forces them to be refreshed from net after 3 hours
     */
    private Rozvrh getFromMemory(LocalDate date) {
        LocalTime updateTime = lastUpdated.get(date);
        if (updateTime == null || updateTime.isAfter(LocalTime.now().minusHours(3))) {
            return saved.get(date);
        } else {
            lastUpdated.remove(date);
            saved.remove(date);
            return null;
        }
    }

    private boolean isInMemory(LocalDate date) {
        LocalTime updateTime = lastUpdated.get(date);
        if (updateTime == null || updateTime.isAfter(LocalTime.now().minusHours(3))) {
            return saved.containsKey(date);
        } else {
            lastUpdated.remove(date);
            saved.remove(date);
            return false;
        }
    }

    /**
     * Clears object's Rozvrh storage - all rozvrhs will have to load from cache and server again.
     */
    public void clearMemory() {
        lastUpdated.clear();
        saved.clear();
    }

    /**
     * Clears memory and requests new schedule from net,
     * if it is cusccessful it clears cache and saves the new one to cache (also caches next, prev., perm., ...) and returns it using {@code onLoaded}
     * listener.
     *
     * @param monday monday identifying week or {@code null} for permanent timetable.
     */
    public void refresh(LocalDate monday, RozvrhListener onLoaded) {
        clearMemory();
        rozvrhLoader.loadRozvrh(monday, result -> {
            if (result.code == SUCCESS) {
                RozvrhCache.clearCache(context);
                RozvrhCache.saveRawRozvrh(monday, result.raw, context);
                cacheCNPP();
                cacheNP(monday);
                putToMemory(monday, result.rozvrh);
                updateLiveData(monday, new RozvrhWrapper(result.rozvrh, result.code, RozvrhWrapper.SOURCE_NET));
            }
            onLoaded.method(new RozvrhWrapper(result.rozvrh, result.code, RozvrhWrapper.SOURCE_NET));
        });
    }

    public void getNextNotificationUpdateTime(TimeListener listener) {
        getRozvrh(Utils.getCurrentMonday(), (rozvrhWrapper) -> {
            int code = rozvrhWrapper.getCode();
            Rozvrh rozvrh = rozvrhWrapper.getRozvrh();
            LocalDateTime updateTime = null;
            int code1 = 0;
            if (rozvrh != null) {
                Rozvrh.GetNCLCTreturnValues values = rozvrh.getNextCurrentLessonChangeTime();
                updateTime = values.localDateTime;
                code1 = values.errCode;
            }
            if (updateTime != null) {
                listener.method(updateTime);
            } else if (code1 == 2) {
                //old schedule -> try the next week
                getRozvrh(Utils.getCurrentMonday().plusWeeks(1), (rozvrhWrapper2) -> {
                    Rozvrh rozvrh1 = rozvrhWrapper2.getRozvrh();
                    listener.method(rozvrh1 == null ? null : rozvrh1.getNextCurrentLessonChangeTime().localDateTime);
                });
            } else {
                listener.method(null);
            }
        });
    }

    public static interface TimeListener {
        public void method(LocalDateTime updateTime);
    }
}

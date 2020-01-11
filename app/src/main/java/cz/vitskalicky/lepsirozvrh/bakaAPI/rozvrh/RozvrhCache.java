package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.joda.time.LocalDate;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.channels.FileLock;

import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.items.RozvrhRoot;

import static cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode.NO_CACHE;
import static cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode.SUCCESS;

public class RozvrhCache {
    public static final String TAG = RozvrhCache.class.getSimpleName();

    /**
     * Saved rozvrh for later faster loading. Saving is performed on background thread and file
     * writing is thread-safe.
     *
     * @param monday monday for week identification, leave null for permanent timetable
     * @param rozvrh string containing timetable xml
     */
    public static void saveRawRozvrh(LocalDate monday, String rozvrh, Context context) {
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
    public static void loadRozvrh(LocalDate monday, RozvrhListener listener, Context context) {
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
                        listener.method(new RozvrhWrapper(null, NO_CACHE, RozvrhWrapper.SOURCE_CACHE)));
                return;
            } catch (Exception e) {
                Log.e(TAG, "Timetable loading failed: error message: " + e.getMessage() + " stack trace:");
                e.printStackTrace();

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.method(new RozvrhWrapper(null, NO_CACHE, RozvrhWrapper.SOURCE_CACHE)));
                return;
            }
            new Handler(Looper.getMainLooper()).post(() ->
                    listener.method(new RozvrhWrapper(root.getRozvrh(), SUCCESS, RozvrhWrapper.SOURCE_CACHE)));
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
}

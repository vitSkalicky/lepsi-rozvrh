package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.vitskalicky.lepsirozvrh.AppSingleton;

public class SchoolsDatabaseAPI {
    public static final String TAG = SchoolsDatabaseAPI.class.getSimpleName();

    public static final String SCHOOLS_DATABASE_URL = "https://sluzby.bakalari.cz/api/v1/municipality/"; //includes the last /


    /**
     * Fetches list ao all places (cities/towns/anything listed on their api) and returns their names
     * using the listener. If action fails, null is returned.
     *
     * @param listListener listener using which data is returned. If action fails, null is returned.
     */
    public static void getCities(RequestQueue requestQueue, Context context, ListListener<String> listListener) {
        StringRequest request = new StringRequest(Request.Method.GET, SCHOOLS_DATABASE_URL, response -> {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(new ByteArrayInputStream(response.getBytes()));

                Element root = document.getDocumentElement();
                root.normalize();

                List<String> list = new LinkedList<>();

                NodeList nodeList = root.getElementsByTagName("name");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    list.add(nodeList.item(i).getTextContent());
                }

                listListener.method(list);
                return;
                /*for (int i = 0; i < root.getChildNodes().getLength(); i++) {
                    Node item = root.getChildNodes().item(i);
                    item.get
                }*/

            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
                listListener.method(null);
                return;
            }
        }, error -> {
            listListener.method(null);
            return;
        });
        requestQueue.add(request);
    }

    /**
     * Fetches list of all schools from Bakaláři api by fetching schools for each letter of Czech alphabet. Might take some time. Returns false if fetching fails.
     *
     * @param database SchoolInfo data will be saved into this database.
     * @param progressBar progress is displayed onto this progressbar unless it is {@code null}.
     * @return RequestQueue used for requests.
     */
    public static RequestQueue getAllSchools(Context context, Listener listener, SchoolsDatabse database, ProgressBar progressBar) {
        final RequestQueue requestQueue = AppSingleton.getInstance(context).getRequestQueue();

        AsyncTask.execute(() -> {
            SchoolDAO dao = database.schoolDAO();

            int schoolsInDatabase = dao.countAllSchools();
            if (schoolsInDatabase > 0) {
                //data already queried before (is wiped on exit)
                new Handler(Looper.getMainLooper()).post(() ->
                listener.onFinished(true));
                return;
            }

            String[] CZchars = {"a", "á", "b", "c", "č", "d", "ď", "e", "é", "ě", "f", "g", "h", "ch", "i", "í", "j", "k", "l", "m", "n", "ň", "o", "ó", "p", "q", "r", "ř", "s", "š", "t", "ť", "u", "ú", "ů", "v", "w", "x", "y", "ý", "z", "ž"};

            int start = CZchars.length;
            AtomicInteger requestsCounter = new AtomicInteger(start);

            new Handler(Looper.getMainLooper()).post(() -> {
                if (progressBar != null) {
                    progressBar.setMax(start);
                    progressBar.setProgress(0);
                }
            });

            for (final String s : CZchars) {
                String url = SCHOOLS_DATABASE_URL + URLEncoder.encode(s);
                try {
                    url = SCHOOLS_DATABASE_URL + URLEncoder.encode(s, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "utf-8 encoding not supported!");
                }

                final String furl = url;

                SchoolRequest request = new SchoolRequest(furl, dao, response -> {

                    decrement(requestsCounter, listener, dao, start, progressBar);
                    Log.d(TAG, s);
                }, error -> {
                    if (error != null && (error.networkResponse == null || error.networkResponse.statusCode != 404)) {
                        Log.e(TAG, "Error while getting schools list: url: " + furl + "\n\n----------\nstack trace");
                        error.printStackTrace();
                    }
                    Log.d(TAG, s);
                    decrement(requestsCounter, listener, dao, start, progressBar);

                });

                requestQueue.add(request);

            }
        });

        return requestQueue;
    }

    public static interface ListListener<T> {
        public void method(List<T> list);
    }

    public static interface Listener {
        public void onFinished(boolean success);
    }

    private static void decrement(AtomicInteger i, Listener listener, SchoolDAO dao, int start, ProgressBar progressBar) {
        int got = i.decrementAndGet();
        if (progressBar != null) {
            new Handler(Looper.getMainLooper()).post(() ->{
                if (Build.VERSION.SDK_INT >= 24) {
                    progressBar.setProgress(start - got, true);
                }else {
                    progressBar.setProgress(start - got);
                }
            });

        }
        if (got == 0) {
            AsyncTask.execute(() -> {
                if (dao.countAllSchools() > 0){
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onFinished(true));
                }else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onFinished(false));
                }
            });
        }

    }
}

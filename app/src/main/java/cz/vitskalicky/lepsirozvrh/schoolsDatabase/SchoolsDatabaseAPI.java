package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.ObjectLockingIndexedCollection;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
     * Fetches list of all schools from Bakaláři api by fetching list of all cities and then list of
     * schools for each one. Might take some time. Returns null if fetching fails.
     * @param collectionListener listener using which data is returned. Returns null if fetching fails.
     * @return RequestQueue used for requests.
     */
    /*public static void getAllSchools(RequestQueue requestQueue, Context context, IndexedCollectionListener<SchoolInfo> collectionListener){
        SchoolsFetcher fetcher = new SchoolsFetcher();
        fetcher.getAllSchools(requestQueue, context, collectionListener);
    }*/

    public static RequestQueue getAllSchools(Context context, IndexedCollectionListener<SchoolInfo> collectionListener, ProgressBar progressBar) {

        RequestQueue requestQueue = AppSingleton.getInstance(context).getRequestQueue();

        IndexedCollection<SchoolInfo> collection = new ObjectLockingIndexedCollection<>();
        collection.addIndex(SuffixTreeIndex.onAttribute(SchoolInfo.STRIPED_NAME));

        String[] CZchars = {"a","á","b","c","č","d","ď","e","é","ě","f","g","h","ch","i","í","j","k","l","m","n","ň","o","ó","p","q","r","ř","s","š","t","ť","u","ú","ů","v","w","x","y","ý","z","ž"};

        int start = CZchars.length;
        AtomicInteger requestsCounter = new AtomicInteger(start);

        if (progressBar != null){
            progressBar.setMax(start);
            progressBar.setProgress(0);
        }

        for (final String s :CZchars) {
            String url = SCHOOLS_DATABASE_URL + URLEncoder.encode(s);
            try {
                url = SCHOOLS_DATABASE_URL + URLEncoder.encode(s, "utf-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "utf-8 encoding not supported!");
            }

            final String furl = url;

            SchoolRequest request = new SchoolRequest(furl, response -> {
                collection.addAll(response);

                decrement(requestsCounter,collectionListener,collection,start,progressBar);
                Log.d(TAG, s);
            },error -> {
                if (error != null && (error.networkResponse == null || error.networkResponse.statusCode != 404)){
                    Log.e(TAG, "Error while getting schools list: url: " + furl + "\n\n----------\nstack trace");
                    error.printStackTrace();
                }
                Log.d(TAG, s);
                decrement(requestsCounter,collectionListener,collection,start,progressBar);

            });

            requestQueue.add(request);

        }
        return requestQueue;
    }

    public static interface ListListener<T> {
        public void method(List<T> list);
    }

    public static interface IndexedCollectionListener<T> {
        public void method(IndexedCollection<T> collection);
    }

    private static void decrement(AtomicInteger i, IndexedCollectionListener<SchoolInfo> collectionListener, IndexedCollection<SchoolInfo> collection, int start,  ProgressBar progressBar){
        int got = i.decrementAndGet();
        if (progressBar != null){
            progressBar.setProgress(start - got);
        }
        if (got == 0){
            collectionListener.method(collection);
        }

    }
}

package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.joda.time.LocalDate;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhRoot;

import static cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode.*;

public class RozvrhRequest extends Request<RozvrhRequest.Result> {
    private static String TAG = RozvrhRequest.class.getSimpleName();

    public static final int TIMEOUT = 10000; //in milliseconds

    private LocalDate monday;
    private Listener successListener;
    private Context context;
    private Listener errorListener;

    private int code;

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
         * One of the {@link cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode} codes;
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

    public static interface Listener {
        public void method(Result result);
    }

    public RozvrhRequest(LocalDate monday, Listener successListener, Listener errorListener, Context context) {
        super(Method.GET, calculateUrl(monday, context), error -> {
            Log.i(TAG, "Getting timetable failed: network error: " + error.getMessage());
            String response = "";
            if (error != null && error.networkResponse != null)
                response = new String(error.networkResponse.data);
            errorListener.method(new Result(null, response, UNREACHABLE));
            return;
        });
        this.monday = monday;
        this.context = context;
        this.successListener = successListener;
        this.errorListener = errorListener;
        Log.d(TAG, "Starting request for week " + (monday == null ? "null" : monday.toString()));

        setRetryPolicy(new DefaultRetryPolicy(TIMEOUT, 1 , 1f));
    }

    public RozvrhRequest(LocalDate monday, Listener allListener, Context context) {
        this(monday, allListener, allListener, context);
    }

    private static String calculateUrl(LocalDate monday, Context context) {
        String strDate;
        if (monday == null) {
            strDate = "perm";
        } else {
            strDate = Utils.dateToString(monday);
        }

        String url = SharedPrefs.getString(context, SharedPrefs.URL);
        String token = Login.getToken(context);
        String pm = Login.isTeacher(context) ? "ucitelrozvrh" : "rozvrh"; //to fetch teacher schedule for teachers
        String fullUrl = url + "?hx=" + token + "&pm=" + pm + "&pmd=" + strDate;
        return fullUrl;
    }

    @Override
    protected Response<Result> parseNetworkResponse(NetworkResponse response) {
        if (response == null){
            return Response.error(new NoConnectionError());
        }
        String responseString = new String(response.data, Charset.forName("UTF-8"));
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new ByteArrayInputStream(response.data));

            Element root = document.getDocumentElement();
            root.normalize();

            Node resultNode = root.getElementsByTagName("result").item(0);
            int result = 0;
            if (resultNode != null){
                result = Integer.parseInt(resultNode.getTextContent());
            }
            if (result == -1) {// Login incorrect
                Log.i(TAG, "Getting timetable failed: login incorrect: url: " + getUrl() + " Date: " + monday + " response:\n" + responseString);
                //turns into an error in #deliverResponse
                return Response.success(new Result(null, responseString, LOGIN_FAILED), HttpHeaderParser.parseCacheHeaders(response));
            } else {
                Serializer serializer = new Persister();
                RozvrhRoot rozvrhRoot = serializer.read(RozvrhRoot.class, responseString);
                if (rozvrhRoot == null || rozvrhRoot.getRozvrh() == null){
                    return Response.success(new Result(null,responseString,UNEXPECTED_RESPONSE), HttpHeaderParser.parseCacheHeaders(response));
                }else {
                    return Response.success(new Result(rozvrhRoot.getRozvrh(),responseString,SUCCESS), HttpHeaderParser.parseCacheHeaders(response));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Getting timetable failed: unexpected response: url: " + getUrl() + " Date: " + monday + " error message: " + e.getMessage() + " response:\n" + responseString);
            e.printStackTrace();
            return Response.success(new Result(null,responseString,UNEXPECTED_RESPONSE), HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected void deliverResponse(Result response) {
        if (response.code == SUCCESS){
            successListener.method(response);
        }else {
            errorListener.method(response);
        }
    }
}

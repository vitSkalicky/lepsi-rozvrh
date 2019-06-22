package cz.vitskalicky.lepsirozvrh.bakaAPI;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.vitskalicky.lepsirozvrh.SharedPrefs;

public class Rozvrh {
    public static interface Listener{
        public void onResponse(int code, Document document);
    }

    public static final int SUCCESS = 0;
    public static final int LOGIN_FAILED = 1;
    public static final int UNEXPECTED_RESPONSE = 2;
    public static final int UNREACHABLE = 3;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");


    /**
     * Gets raw xml document from the server.
     * @param mondayDate Date of monday of the requested week. If {@code null}, permanent timetable is returned.
     * @param listener Listener for returning data
     */
    private static void fetchXml(Calendar mondayDate, Listener listener, RequestQueue requestQueue, Context context){
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
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(new ByteArrayInputStream(response.getBytes()));

                Element root = document.getDocumentElement();
                root.normalize();

                int result = Integer.parseInt(root.getElementsByTagName("result").item(0).getTextContent());

                if (result == -1){// Login incorrect
                    System.err.println("Getting timetable failed: login incorrect: url: " + url + " Date: " + strDate + " response:\n" + response);
                    listener.onResponse(LOGIN_FAILED, document);
                    return;
                }

                listener.onResponse(SUCCESS, document);
                return;
            } catch (ParserConfigurationException | IOException | SAXException | NullPointerException | NumberFormatException e) {
                System.err.println("Getting timetable failed: unexpected response: url: " + url + " Date: " + strDate + " response:\n" + response);
                e.printStackTrace();
                listener.onResponse(UNEXPECTED_RESPONSE, null);
            }
        },error -> {
            System.err.println("Getting timetable failed: network error: " + error.getMessage());
            listener.onResponse(UNREACHABLE, null);
            return;
        });
        requestQueue.add(request);
    }
}

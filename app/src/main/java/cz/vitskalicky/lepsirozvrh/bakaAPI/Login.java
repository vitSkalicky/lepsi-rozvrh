package cz.vitskalicky.lepsirozvrh.bakaAPI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.activity.LoginActivity;
import cz.vitskalicky.lepsirozvrh.activity.MainActivity;
import cz.vitskalicky.lepsirozvrh.activity.WelcomeActivity;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhRequest;

public class Login {
    private static String TAG = Login.class.getSimpleName();

    /**
     * ResponseListener for returning login data.
     */
    public static interface Listener{
        public void onResponse(int code, String data);
    }

    public static final int SUCCESS = 0; // data: token
    public static final int WRONG_USERNAME = 1; // data: response
    public static final int WRONG_PASSWORD = 2; // data: response
    public static final int UNEXPECTER_RESPONSE = 3; // data: response
    public static final int SERVER_UNREACHABLE = 4; // data: message
    public static final int ROZVRH_DISABLED = 5; // data: response


    /**
     * Logs in user and returns its token through listener. Credentials are saved (if login successful).
     * when finished {@link Listener#onResponse(int, String)} is called with {@code code} being one of constants above
     * (in this class) and {@code data} being either token, server response or error message (see constants comments)
     * @param url Bakaláři login site url
     * @param username user's username
     * @param password user's password
     * @param listener listener for returning data
     * @param context app context
     */
    public static void login(String url, String username, String password, Listener listener, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);

        if (username.equals("")){
            listener.onResponse(WRONG_USERNAME, null);
            return;
        }

        //URL encode username
        String s = username;
        try {
            s = URLEncoder.encode(username, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String enUsername = s;

        String uniUrl = unifyUrl(url);

        //get salts, etc.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, uniUrl + "?gethx=" + enUsername, response -> {

            //request successful
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(new ByteArrayInputStream(response.getBytes()));

                Element root = doc.getDocumentElement();
                root.normalize();
                int res = Integer.parseInt(doc.getElementsByTagName("res").item(0).getTextContent());

                if (res == 2){ //wrong username
                    Log.i(TAG, "Login failed: wrong username - username: " + username + " url: " + uniUrl + "?gethx=" + enUsername + " response:\n" + response);
                    listener.onResponse(WRONG_USERNAME, response);
                    return;
                }

                String typ = root.getElementsByTagName("typ").item(0).getTextContent();
                String ikod = root.getElementsByTagName("ikod").item(0).getTextContent();
                String salt = root.getElementsByTagName("salt").item(0).getTextContent();

                String passwordHash = calculatePasswordHash(salt,ikod,typ,password);
                String token = calculateToken(username, passwordHash);

                // password check - make empty request

                StringRequest passwordCheck = new StringRequest(Request.Method.GET, uniUrl + "?hx=" + token + "&pm=login", response1 -> {

                    try {
                        DocumentBuilderFactory dbFactory1 = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder1 = dbFactory1.newDocumentBuilder();
                        Document doc1 = dBuilder1.parse(new ByteArrayInputStream(response1.getBytes()));

                        Element root1 = doc1.getDocumentElement();
                        root1.normalize();
                        int result = Integer.parseInt(doc1.getElementsByTagName("result").item(0).getTextContent());

                        if (result == -1) {
                            //password incorrect
                            Log.i(TAG, "Login failed: wrong password - username: " + username + " url: " + uniUrl + "?hx=<token>" + "&pm=login" + " response:\n" + response1);
                            listener.onResponse(WRONG_PASSWORD, response1);
                            return;
                        }else {
                            //password correct (hopefully)

                            String modules = doc1.getElementsByTagName("moduly").item(0).getTextContent();
                            if (!modules.contains("rozvrh")){
                                // Rozvrh module not enabled
                                Log.i(TAG, "Login failed: rozvrh not enabled - username: " + username + " url: " + uniUrl + "?hx=<token>" + "&pm=login" + " response:\n" + response1);
                                listener.onResponse(ROZVRH_DISABLED,response1);
                                return;
                            }

                            String name = "";
                            String type = "";
                            try {
                                name = doc1.getElementsByTagName("jmeno").item(0).getTextContent();
                                type = doc1.getElementsByTagName("typ").item(0).getTextContent();
                            }catch (Exception e){
                                Log.e(TAG, "Login failed: user's name not found, setting to \"\". response:\n" + response1);
                            }

                            // save credentials
                            SharedPrefs.setString(context,SharedPrefs.USERNAME, username);
                            SharedPrefs.setString(context, SharedPrefs.PASSWORD_HASH, passwordHash);
                            SharedPrefs.setString(context, SharedPrefs.URL, uniUrl);
                            SharedPrefs.setString(context,SharedPrefs.NAME, name);
                            SharedPrefs.setString(context, SharedPrefs.TYPE, type);

                            listener.onResponse(SUCCESS,token);
                            return;
                        }

                    } catch (ParserConfigurationException | IOException | SAXException | NullPointerException | NumberFormatException e) {
                        Log.e(TAG, "Login failed: unexpected server response. url: " + uniUrl + "?hx=<token>" + "&pm=login" + " response:\n" + response1);
                        e.printStackTrace();
                        listener.onResponse(UNEXPECTER_RESPONSE, response1);
                    }
                }, error -> {
                    Log.i(TAG, "Login failed: connection error: url: " + uniUrl + "?hx=<token>" + " error message: " + error.getMessage());
                    error.printStackTrace();
                    listener.onResponse(SERVER_UNREACHABLE, error.getMessage());
                });

                passwordCheck.setRetryPolicy(new DefaultRetryPolicy(RozvrhRequest.TIMEOUT, 1, 1f));
                queue.add(passwordCheck);

            } catch (ParserConfigurationException | IOException | SAXException | NullPointerException | NumberFormatException e) {
                Log.e(TAG, "Login failed: unexpected server response. url: " + uniUrl + "?gethx=" + enUsername + " response:\n" + response);
                e.printStackTrace();
                listener.onResponse(UNEXPECTER_RESPONSE, response);
            }
        }, error -> {
            Log.i(TAG,"Login failed: connection error: url: " + uniUrl + "?gethx=" + enUsername + " error message: " + error.getMessage());
            error.printStackTrace();
            listener.onResponse(SERVER_UNREACHABLE, error.getMessage());
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RozvrhRequest.TIMEOUT, 1, 1f));

        queue.add(stringRequest);
    }

    /**
     * Logs out user (deletes credentials)
     */
    public static void logout(Context context){
        SharedPrefs.remove(context, SharedPrefs.USERNAME);
        SharedPrefs.remove(context, SharedPrefs.PASSWORD_HASH);
        SharedPrefs.remove(context, SharedPrefs.URL);
        SharedPrefs.remove(context, SharedPrefs.NAME);
        RozvrhAPI.clearCache(context);
        AppSingleton.getInstance(context).getRozvrhAPI().clearMemory();
    }

    /**
     * Checks if user is logged in or has seen the welcome screen (where crash reports are
     * enabled/disabled), the starts the corresponding activity (if it isn't already started).
     * <code>finish()</code> <b>won't</b> be called on the current activity.
     *
     * @return An activity which is being started or <code>null</code> if no activity will be started.
     */
    public static Class<? extends Activity> checkLogin(Activity currentActivity){
        Context ctx = currentActivity;
        boolean isLoggedIn = !getToken(ctx).isEmpty();
        boolean seenWelcome = SharedPrefs.containsPreference(ctx, R.string.PREFS_SEND_CRASH_REPORTS);

        if (!seenWelcome && !(currentActivity instanceof WelcomeActivity)){
            Intent intent = new Intent(ctx, WelcomeActivity.class);
            ctx.startActivity(intent);
            return WelcomeActivity.class;
        }
        if (!isLoggedIn && !(currentActivity instanceof LoginActivity)){
            Intent intent = new Intent(ctx, LoginActivity.class);
            ctx.startActivity(intent);
            currentActivity.finish();
            return LoginActivity.class;
        }
        if (!(currentActivity instanceof MainActivity)){
            Intent intent = new Intent(ctx, MainActivity.class);
            ctx.startActivity(intent);
            currentActivity.finish();
            return MainActivity.class;
        }
        return null;
    }

    /**
     * Claculates token from saved credentials. !!! returns empty string if the user is not logged in !!!
     */
    public static String getToken(Context context){
        if (!SharedPrefs.contains(context, SharedPrefs.USERNAME) || !SharedPrefs.contains(context, SharedPrefs.PASSWORD_HASH)){
            //not logged in
            Log.e(TAG, "Getting token failed: lot logged in - returning \"\"");
            return "";
        }
        return calculateToken(SharedPrefs.getString(context,SharedPrefs.USERNAME), SharedPrefs.getString(context, SharedPrefs.PASSWORD_HASH));
    }

    /**
     * Calculates token valid for current day for Bakaláři API
     */
    public static String calculateToken(String username, String passwordHash){
        LocalDate today = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
        String strDate = dtf.print(today);
        String token = sha512("*login*" + username + "*pwd*" + passwordHash + "*sgn*ANDR" + strDate);
        token = token.replace("/", "_");
        token = token.replace("+", "-");
        return token;
    }

    /**
     * Calculates password hash for Bakaláři API
     */
    public static String calculatePasswordHash(String salt, String ikod, String typ, String password){
        return sha512(salt + ikod + typ + password);
    }

    /**
     * Unifies school url to http(s)://xxx.school.cz/yyy/login.aspx
     *
     * https://bakalari.school.cz -> https://bakalari.school.cz/login.aspx
     * https://bakalari.school.cz/bakaweb/login.aspx -> https://bakalari.school.cz/bakaweb/login.aspx (no change)
     */
    private static String unifyUrl(String url){
        if (url.endsWith("/login.aspx"))
            return url;
        else if (url.endsWith("/"))
            return url + "login.aspx";
        else
            return url + "/login.aspx";
    }

    /**
     * Calculates SHA-512 Base64 encoded hash of given string
     */
    private static String sha512(String text){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(text.getBytes());
            byte[] bytes = md.digest();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }

    }
}

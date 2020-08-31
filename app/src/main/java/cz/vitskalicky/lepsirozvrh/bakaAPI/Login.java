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
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.activity.LoginActivity;
import cz.vitskalicky.lepsirozvrh.activity.MainActivity;
import cz.vitskalicky.lepsirozvrh.activity.WelcomeActivity;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhCache;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhRequest;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;
import cz.vitskalicky.lepsirozvrh.widget.WidgetProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Login {
    private static String TAG = Login.class.getSimpleName();

    /**
     * ResponseListener for returning login data.
     */
    public static interface Listener{
        public void onResponse(int code);
    }

    public static final int SUCCESS = 0; // data: token
    public static final int WRONG_LOGIN = 1; // data: response
    public static final int UNEXPECTER_RESPONSE = 3; // data: response
    public static final int SERVER_UNREACHABLE = 4; // data: message
    public static final int ROZVRH_DISABLED = 5; // data: response

    /**
     * Logs in user and returns status through listener. Credentials are saved (if login successful).
     * when finished {@link Listener#onResponse(int)} is called with {@code code} being one of constants above
     * (in this class).
     * @param url Bakaláři base url (eg. https://bakalari.gpisnicka.cz/bakaweb/)
     * @param username user's username
     * @param password user's password
     * @param listener listener for returning status
     * @param context app context
     */
    public static void login(String url, String username, String password, Listener listener, Context context){
        SharedPrefs.setString(context, SharedPrefs.URL, unifyUrl(url));
        Retrofit retrofit = ((MainApplication)context.getApplicationContext()).getRetrofit();

        LoginAPInterface apiInterface = retrofit.create(LoginAPInterface.class);

        apiInterface.firstLogin("ANDR", "password", username, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()){
                    String refreshToken = response.body().refresh_token;
                    String accessToken = response.body().access_token;

                    SharedPrefs.setString(context, SharedPrefs.REFRESH_TOKEN, refreshToken);
                    SharedPrefs.setString(context, SharedPrefs.ACCEESS_TOKEN, accessToken);
                    SharedPrefs.setString(context, SharedPrefs.ACCESS_EXPIRES, LocalDateTime.now().plusSeconds(response.body().expires_in).toString(ISODateTimeFormat.dateTime()));

                    apiInterface.getUser().enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                            if(response.isSuccessful()){
                                SharedPrefs.setString(context, SharedPrefs.NAME, response.body().fullName);
                                SharedPrefs.setString(context, SharedPrefs.TYPE, response.body().UserType);
                                listener.onResponse(SUCCESS);
                            }else {
                                Log.e(TAG, "Unexpected user login response: " + response.toString());
                                listener.onResponse(UNEXPECTER_RESPONSE);
                            }
                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {
                            Log.e(TAG, t.toString());
                            t.printStackTrace();
                            listener.onResponse(SERVER_UNREACHABLE);
                        }
                    });
                }else {
                    Log.e(TAG, "Login failed: " + response.toString());
                    listener.onResponse(WRONG_LOGIN);

                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, t.toString());
                t.printStackTrace();
                listener.onResponse(SERVER_UNREACHABLE);
            }
        });
    }

    /**
     * refreshes login token
     * */
    public static void refreshToken(Listener listener, Context context){
        Retrofit retrofit = ((MainApplication)context.getApplicationContext()).getRetrofit();
        LoginAPInterface apiInterface = retrofit.create(LoginAPInterface.class);

        apiInterface.refreshLogin("ANDR", "refresh_token", SharedPrefs.getString(context, SharedPrefs.REFRESH_TOKEN)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()){
                    String refreshToken = response.body().refresh_token;
                    String accessToken = response.body().access_token;

                    SharedPrefs.setString(context, SharedPrefs.REFRESH_TOKEN, refreshToken);
                    SharedPrefs.setString(context, SharedPrefs.ACCEESS_TOKEN, accessToken);
                    SharedPrefs.setString(context, SharedPrefs.ACCESS_EXPIRES, LocalDateTime.now().plusSeconds(response.body().expires_in).toString(ISODateTimeFormat.dateTime()));

                    listener.onResponse(SUCCESS);
                }else {
                    Log.e(TAG, "Refresh failed: " + response.toString());
                    listener.onResponse(WRONG_LOGIN);

                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, t.toString());
                t.printStackTrace();
                listener.onResponse(SERVER_UNREACHABLE);
            }
        });
    }

    public static void refreshTokenIfNeeded(Listener listener, Context context){
        if (getAccessToken(context).isEmpty()){
            refreshToken(listener, context);
        }
    }

    /**
     * Returns a valid access token or an empty string.
     */
    public static String getAccessToken(Context context){
        String expiresStr = SharedPrefs.getString(context, SharedPrefs.ACCESS_EXPIRES);
        if (expiresStr.isEmpty())
            return "";
        LocalDateTime expires = LocalDateTime.parse(expiresStr, ISODateTimeFormat.dateTimeParser());
        if (expires.isBefore(LocalDateTime.now()))
            return "";
        return SharedPrefs.getString(context, SharedPrefs.ACCEESS_TOKEN);
    }

    /**
     * Logs out user (deletes credentials)
     */
    public static void logout(Context context){
        SharedPrefs.remove(context, SharedPrefs.USERNAME);
        SharedPrefs.remove(context, SharedPrefs.REFRESH_TOKEN);
        SharedPrefs.remove(context, SharedPrefs.ACCEESS_TOKEN);
        SharedPrefs.remove(context, SharedPrefs.ACCESS_EXPIRES);
        SharedPrefs.remove(context, SharedPrefs.URL);
        SharedPrefs.remove(context, SharedPrefs.NAME);
        RozvrhCache.clearCache(context);
        PermanentNotification.update(null, 0, context);
        WidgetProvider.updateAll(null, context);
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
        boolean isLoggedIn = !SharedPrefs.getString(currentActivity, SharedPrefs.REFRESH_TOKEN).isEmpty();
        boolean seenWelcome = SharedPrefs.containsPreference(ctx, R.string.PREFS_SEND_CRASH_REPORTS);

        if (!seenWelcome && !(currentActivity instanceof WelcomeActivity)){
            Intent intent = new Intent(ctx, WelcomeActivity.class);
            ctx.startActivity(intent);
            return WelcomeActivity.class;
        }
        if (!isLoggedIn && !(currentActivity instanceof LoginActivity)){
            Intent intent = new Intent(ctx, LoginActivity.class);
            ctx.startActivity(intent);
            return LoginActivity.class;
        }
        if (!(currentActivity instanceof MainActivity)){
            Intent intent = new Intent(ctx, MainActivity.class);
            ctx.startActivity(intent);
            return MainActivity.class;
        }
        return null;
    }

    /**
     * Whether to show teacher's or students rozvrh (each is fetched and displayed slightly differently)
     * @return {@code true} if the user logged in is a teacher or {@code false} if not (then it is a student or a parent)
     */
    public static boolean isTeacher(Context context){
        String type = SharedPrefs.getString(context, SharedPrefs.TYPE);
        if (type.equals("teacher")){
            return true;
        }else {
            return false;
        }
    }

    /**
     * Removes /next/login.aspx
     */
    private static String unifyUrl(String url){
        if (url.endsWith(".aspx"))
            url = url.substring(0, url.length() - 5);
        if (url.endsWith("login")){
            url = url.substring(0, url.length() - 5);
            if (url.endsWith("next/"))
                url = url.substring(0, url.length() - 5);
        }
        if (!url.endsWith("/"))
            url += "/";
        return url;
    }
}

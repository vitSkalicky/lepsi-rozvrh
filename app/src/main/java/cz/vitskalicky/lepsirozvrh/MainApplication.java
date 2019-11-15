package cz.vitskalicky.lepsirozvrh;

import android.app.Application;

import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Sentry (crash report) client
        if (SharedPrefs.getBooleanPreference(this, R.string.PREFS_SEND_CRASH_REPORTS)){
            enableSentry();
        }else{
            diableSentry();
        }
    }

    public void enableSentry(){
        Sentry.init("https://d13d732d380444f5bed7487cfea65814@sentry.io/1820627", new AndroidSentryClientFactory(this));
    }

    public void diableSentry(){
        Sentry.close();
    }
}
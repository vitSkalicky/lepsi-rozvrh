package cz.vitskalicky.lepsirozvrh;

import android.app.Application;
import android.content.Context;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.BreadcrumbBuilder;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Sentry (crash report) client
        Sentry.close();
        Sentry.init("https://d13d732d380444f5bed7487cfea65814@sentry.io/1820627", new AndroidSentryClientFactory(this));
    }
}
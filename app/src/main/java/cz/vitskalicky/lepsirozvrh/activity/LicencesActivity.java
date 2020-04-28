package cz.vitskalicky.lepsirozvrh.activity;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.widget.Toolbar;

import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import cz.vitskalicky.lepsirozvrh.R;

public class LicencesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licences);

        setTitle(R.string.oss_licences);

        WebView webView = findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/open_source_licenses.html");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Cyanea.getInstance().getMenuIconColor());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}

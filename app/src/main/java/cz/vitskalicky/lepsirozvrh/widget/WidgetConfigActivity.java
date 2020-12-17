package cz.vitskalicky.lepsirozvrh.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.donations.Donations;

/**
 * A base class for Widget configuration activities taking care of saving the data, OK, button and spinner.
 */
public abstract class WidgetConfigActivity extends AppCompatActivity implements WidgetThemeFragment.CallbackListener {
    private static final String TAG = WidgetConfigActivity.class.getSimpleName();

    protected WidgetThemeFragment widgetThemeFragment;
    protected Button okButton;
    protected Donations donations;

    int widgetID = 0;
    boolean isWidgetIDSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createContentView();

        donations = new Donations(this, this, () -> {
            //on sponsor change
            if (widgetThemeFragment != null){
                widgetThemeFragment.updateSponsor();
            }
        });

        if (widgetThemeFragment == null) {
            widgetThemeFragment = (WidgetThemeFragment) getSupportFragmentManager().findFragmentById(R.id.widgetThemeFragment);
            if (widgetThemeFragment == null) {
                throw new RuntimeException("Layout must contain a WidgetThemeFragment with id \"widgetThemeFragment\" or the protected field \"widgetThemeFragment\" must be set in \"createContentView()\"");
            }
        }
        widgetThemeFragment.init(donations);

        if (okButton == null) {
            okButton = findViewById(R.id.buttonOK);
            if (okButton == null) {
                throw new RuntimeException("Layout must contain a Button with id \"okButton\" or the protected field \"okButton\" must be set in \"createContentView()\"");
            }
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            isWidgetIDSet = true;
            widgetThemeFragment.setWidgetID(widgetID);
        }

        okButton.setOnClickListener(v -> {
            widgetThemeFragment.saveConfig();
            if (isWidgetIDSet) {
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
                setResult(RESULT_OK, resultValue);
                AppSingleton.getInstance(this).getRozvrhAPI().getRozvrh(Utils.getCurrentMonday(), rozvrhWrapper -> {
                    WidgetProvider.update(widgetID, rozvrhWrapper.getOldRozvrh() == null ? null : rozvrhWrapper.getOldRozvrh().getWidgetDiaplayValues(5, this), this);
                    finish();
                });
            } else {
                finish();
            }
        });

        widgetThemeFragment.setCallbackListener(this);
    }

    protected abstract void createContentView();

    @Override
    public void onBackPressed() {
        if (isWidgetIDSet) {
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_CANCELED, resultValue);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        donations.release();
    }
}

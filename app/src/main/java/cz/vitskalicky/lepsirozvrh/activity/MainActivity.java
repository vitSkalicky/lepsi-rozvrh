package cz.vitskalicky.lepsirozvrh.activity;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;

import cz.vitskalicky.lepsirozvrh.AppSingleton;
import cz.vitskalicky.lepsirozvrh.DisplayInfo;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.bakaAPI.Login;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;
import cz.vitskalicky.lepsirozvrh.settings.SettingsActivity;
import cz.vitskalicky.lepsirozvrh.view.RozvrhTableFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_TIMER = TAG + "-timer";

    public static final String EXTRA_JUMP_TO_TODAY = MainActivity.class.getCanonicalName() + ".JUMP_TO_TODAY";

    Context context = this;

    Toolbar bottomAppBar;

    RozvrhTableFragment rtFragment;

    ImageButton ibSettings;
    ImageButton ibPrev;
    ImageButton ibCurrent;
    ImageButton ibPermanent;
    ImageButton ibNext;
    ImageButton ibRefresh;
    ProgressBar progressBar;

    RozvrhAPI rozvrhAPI;

    TextView infoLine;
    DisplayInfo displayInfo;

    int week = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLogin();

        rozvrhAPI = AppSingleton.getInstance(context).getRozvrhAPI();

        bottomAppBar = findViewById(R.id.toolbar);
        setSupportActionBar(bottomAppBar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(false);

        displayInfo = new DisplayInfo();
        infoLine = findViewById(R.id.infoLine);
        displayInfo.addOnMessageChangeListener((oldMessage, newMessage) -> {
            setInfoText(newMessage);
            if (displayInfo.getErrorMessage() != null){
                TooltipCompat.setTooltipText(ibRefresh, displayInfo.getErrorMessage());
            }else {
                TooltipCompat.setTooltipText(ibRefresh, getText(R.string.refresh));
            }
        });

        rtFragment = (RozvrhTableFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        rtFragment.init(rozvrhAPI, displayInfo);

        ibSettings = findViewById(R.id.settings);
        ibPrev = findViewById(R.id.prev);
        ibCurrent = findViewById(R.id.curent);
        ibPermanent = findViewById(R.id.permanent);
        ibNext = findViewById(R.id.next);
        ibRefresh = findViewById(R.id.refresh);
        progressBar = findViewById(R.id.progressBar);

        rtFragment.createViews();

        ibSettings.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        TooltipCompat.setTooltipText(ibSettings, getText(R.string.settings));
        TooltipCompat.setTooltipText(ibPrev, getText(R.string.prev_week));
        TooltipCompat.setTooltipText(ibCurrent, getText(R.string.current_week));
        TooltipCompat.setTooltipText(ibPermanent, getText(R.string.permanent_schedule));
        TooltipCompat.setTooltipText(ibNext, getText(R.string.next_week));
        TooltipCompat.setTooltipText(ibRefresh, getText(R.string.refresh));
        ibPrev.setOnClickListener(v -> {
            week--;
            rtFragment.displayWeek(week, false);
            showHideButtons();
        });
        ibNext.setOnClickListener(v -> {
            week++;
            rtFragment.displayWeek(week, false);
            showHideButtons();
        });
        ibCurrent.setOnClickListener(v -> {
            week = 0;
            rtFragment.displayWeek(week, true);
            showHideButtons();
        });
        ibPermanent.setOnClickListener(v -> {
            week = Integer.MAX_VALUE;
            rtFragment.displayWeek(week, false);
            showHideButtons();
        });
        ibRefresh.setOnClickListener(v -> {
            rtFragment.refresh();
            //rtFragment.displayWeek(week);
        });
        displayInfo.addOnLoadingStateChangeListener((oldState, newState) -> {
            if (newState == DisplayInfo.LOADED){
                ibRefresh.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                ibRefresh.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_refresh_black_24));
            } else if (newState == DisplayInfo.ERROR) {
                ibRefresh.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                ibRefresh.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_refresh_problem_black_24dp));
            } else if (newState == DisplayInfo.LOADING){
                ibRefresh.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        ibSettings.setOnLongClickListener(v -> {
            File dir = context.getFilesDir();

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File fileDir, String name) {
                    if (fileDir == dir) {
                        return true;
                    }
                    return false;
                }
            };

            String[] fileNames = dir.list(filter);

            for (String item : fileNames) {
                context.deleteFile(item);
            }
            return true;
        });

        rtFragment.createViews();
        if (savedInstanceState == null)
            week = 0;
        else
            week = savedInstanceState.getInt(STATE_WEEK, 0);

        showHideButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();

        checkLogin();

        if (!SharedPrefs.containsPreference(context, R.string.PREFS_SHOW_INFO_LINE) || SharedPrefs.getBooleanPreference(context, R.string.PREFS_SHOW_INFO_LINE)){
            infoLine.setVisibility(View.VISIBLE);
        }else {
            infoLine.setVisibility(View.GONE);
        }

        Intent intent = getIntent();
        boolean jumpToToday = intent.getBooleanExtra(EXTRA_JUMP_TO_TODAY,false);
        if (jumpToToday){
            rtFragment.displayWeek(0, true);
            intent.removeExtra(EXTRA_JUMP_TO_TODAY);
        }else {
            rtFragment.displayWeek(week, true);
        }
        boolean fromNotification = intent.getBooleanExtra(PermanentNotification.EXTRA_NOTIFICATION, false);
        intent.removeExtra(PermanentNotification.EXTRA_NOTIFICATION);
        if (fromNotification){
            PermanentNotification.showInfoDialog(context, false);
        }
    }

    /**
     * shows/hides buttons accordingly to current state. My english is bad, but you got the point.
     */
    private void showHideButtons(){
        if (week == 0){
            ibPermanent.setVisibility(View.VISIBLE);
            ibCurrent.setVisibility(View.GONE);
        }else{
            ibPermanent.setVisibility(View.GONE);
            ibCurrent.setVisibility(View.VISIBLE);
        }
        if (week == Integer.MAX_VALUE){
            ibNext.setVisibility(View.GONE);
            ibPrev.setVisibility(View.GONE);
        } else {
            ibPrev.setVisibility(View.VISIBLE);
            ibNext.setVisibility(View.VISIBLE);
        }
    }

    private void setInfoText(String text){
        infoLine.setText(text);
    }

    public void checkLogin(){
        if (Login.checkLogin(this) != null){
            if (rozvrhAPI != null){
                rozvrhAPI.clearMemory();
            }
            finish();
        }
    }

    private static final String STATE_WEEK = "week";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_WEEK, week);
        RozvrhAPI.clearOldCache(context);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}

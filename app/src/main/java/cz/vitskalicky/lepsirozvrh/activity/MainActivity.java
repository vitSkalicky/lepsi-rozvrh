package cz.vitskalicky.lepsirozvrh.activity;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;

import cz.vitskalicky.lepsirozvrh.DisplayInfo;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.bakaAPI.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.view.RozvrhTableFragment;

public class MainActivity extends AppCompatActivity {

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

        rozvrhAPI = new RozvrhAPI(Volley.newRequestQueue(context), context);

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
            Toast.makeText(context, "Settings", Toast.LENGTH_SHORT).show();
        });
        ibPrev.setOnClickListener(v -> {
            week--;
            rtFragment.displayWeek(week);
            ibPermanent.setVisibility(View.GONE);
            ibCurrent.setVisibility(View.VISIBLE);
        });
        ibNext.setOnClickListener(v -> {
            week++;
            rtFragment.displayWeek(week);
            ibPermanent.setVisibility(View.GONE);
            ibCurrent.setVisibility(View.VISIBLE);
        });
        ibCurrent.setOnClickListener(v -> {
            week = 0;
            rtFragment.displayWeek(week);
            ibPermanent.setVisibility(View.VISIBLE);
            ibCurrent.setVisibility(View.GONE);
            ibNext.setVisibility(View.VISIBLE);
            ibPrev.setVisibility(View.VISIBLE);
        });
        ibPermanent.setOnClickListener(v -> {
            week = Integer.MAX_VALUE;
            rtFragment.displayWeek(week);
            ibPermanent.setVisibility(View.GONE);
            ibCurrent.setVisibility(View.VISIBLE);
            ibNext.setVisibility(View.GONE);
            ibPrev.setVisibility(View.GONE);
        });
        ibRefresh.setOnClickListener(v -> {
            rtFragment.refresh();
            rtFragment.displayWeek(week);
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

        rtFragment.createViews();
        week = 0;
        rtFragment.displayWeek(week);
    }

    private void setInfoText(String text){
        infoLine.setText(text);
    }
}

package cz.vitskalicky.lepsirozvrh.activity;

import android.content.Context;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;

import org.joda.time.Duration;
import org.joda.time.LocalDate;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.bakaAPI.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
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

    RozvrhAPI rozvrhAPI;

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

        rtFragment = (RozvrhTableFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        rtFragment.init(rozvrhAPI);

        ibSettings = findViewById(R.id.settings);
        ibPrev = findViewById(R.id.prev);
        ibCurrent = findViewById(R.id.curent);
        ibPermanent = findViewById(R.id.permanent);
        ibNext = findViewById(R.id.next);
        ibRefresh = findViewById(R.id.refresh);

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
            rozvrhAPI.refresh();
            rtFragment.displayWeek(week);
        });

        rtFragment.createViews();
        week = 0;
        rtFragment.displayWeek(week);
    }
}

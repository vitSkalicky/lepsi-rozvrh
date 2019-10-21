package cz.vitskalicky.lepsirozvrh.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.fragment.app.Fragment;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.DisplayInfo;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;
import static cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class RozvrhTableFragment extends Fragment {
    public static final String TAG = RozvrhTableFragment.class.getSimpleName();
    public static final String TAG_TIMER = TAG + "-timer";

    private View view;
    private RozvrhLayout rozvrhLayout;

    private DisplayInfo displayInfo;

    private LocalDate week = null;
    private int weekIndex = 0; //what week is it from now (0: this, 1: next, -1: last, Integer.MAX_VALUE: permanent)
    private boolean cacheSuccessful = false;
    private boolean offline = false;
    private RozvrhAPI rozvrhAPI = null;


    public RozvrhTableFragment() {
        // Required empty public constructor
    }

    /**
     * must be called
     */
    public void init(RozvrhAPI rozvrhAPI, DisplayInfo displayInfo) {
        this.rozvrhAPI = rozvrhAPI;
        this.displayInfo = displayInfo;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //debug timing: Log.d(TAG_TIMER, "onCreateView start " + Utils.getDebugTime());
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_rozvrh_table, container, false);

        rozvrhLayout = view.findViewById(R.id.rozvrhLayout);

        //debug timing: Log.d(TAG_TIMER, "onCreateView end " + Utils.getDebugTime());
        return view;
    }


    private int netCode = -1;

    /**
     * @param weekIndex index of week to display relative to now (0 = this week, 1 = next, -1 = previous) or {@code Integer.MAX_VALUE} for permanent
     */
    public void displayWeek(int weekIndex) {
        //debug timing: Log.d(TAG_TIMER, "displayWeek start " + Utils.getDebugTime());

        this.weekIndex = weekIndex;
        if (weekIndex == Integer.MAX_VALUE)
            week = null;
        else
            week = Utils.getDisplayWeekMonday(getContext()).plusWeeks(weekIndex);

        final LocalDate finalWeek = week;

        displayInfo.setLoadingState(DisplayInfo.LOADING);
        cacheSuccessful = false;
        displayInfo.setMessage(Utils.getfl10nedWeekString(weekIndex, getContext()));
        if (offline) {
            displayInfo.setMessage(displayInfo.getMessage() + " (" + getString(R.string.info_offline) + ")");
        }
        netCode = -1;
        Rozvrh item = rozvrhAPI.get(week, (code, rozvrh) -> {
            //onCachLoaded
            // have to make sure that net was not faster
            if (netCode != SUCCESS)
                onCacheResponse(code, rozvrh, finalWeek);
            if (netCode != -1 && netCode != SUCCESS) {
                onNetResponse(netCode, null, finalWeek);
            }
        }, (code, rozvrh) -> {
            netCode = code;
            onNetResponse(code, rozvrh, finalWeek);
        });
        if (item != null) {
            rozvrhLayout.setRozvrh(item);
            if (offline) {
                displayInfo.setLoadingState(DisplayInfo.ERROR);
            } else {
                displayInfo.setLoadingState(DisplayInfo.LOADED);
            }
        } else {
            rozvrhLayout.empty();
        }
        //debug timing: Log.d(TAG_TIMER, "displayWeek end " + Utils.getDebugTime());
    }

    private void onNetResponse(int code, Rozvrh rozvrh, final LocalDate finalWeek) {
        //check if fragment was not removed while loading
        if (getContext() == null) {
            return;
        }
        if (week != finalWeek) {
            return;
        }
        if (rozvrh != null) {
            rozvrhLayout.setRozvrh(rozvrh);
        }
        //onNetLoaded
        if (code == SUCCESS) {
            if (offline) {
                rozvrhAPI.clearMemory();
            }
            offline = false;
            displayInfo.setMessage(Utils.getfl10nedWeekString(weekIndex, getContext()));
            displayInfo.setLoadingState(DisplayInfo.LOADED);
        } else {
            offline = true;
            displayInfo.setLoadingState(DisplayInfo.ERROR);
            if (cacheSuccessful) {
                displayInfo.setMessage(Utils.getfl10nedWeekString(weekIndex, getContext()) + " (" + getString(R.string.info_offline) + ")");
            } else if (code == UNREACHABLE) {
                displayInfo.setMessage(getString(R.string.info_unreachable));
            } else if (code == UNEXPECTED_RESPONSE) {
                displayInfo.setMessage(getString(R.string.info_unexpected_response));
            } else if (code == LOGIN_FAILED) {
                displayInfo.setMessage(getString(R.string.info_login_failed));
            }
        }
    }

    private void onCacheResponse(int code, Rozvrh rozvrh, final LocalDate finalWeek) {
        //check if fragment was not removed while loading
        if (getContext() == null) {
            return;
        }
        if (week != finalWeek) {
            return;
        }
        if (code == SUCCESS) {
            cacheSuccessful = true;
            rozvrhLayout.setRozvrh(rozvrh);
        }
    }

    public void refresh() {
        final LocalDate finalWeek = week;
        displayInfo.setLoadingState(DisplayInfo.LOADING);
        cacheSuccessful = false;

        rozvrhAPI.refresh(week, (code, rozvrh) -> {
            onNetResponse(code, rozvrh, finalWeek);
        });
    }

    public void createViews(){
        rozvrhLayout.createViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        rozvrhLayout.highlightCurrentLesson();
    }
}

package cz.vitskalicky.lepsirozvrh.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.DisplayInfo;
import cz.vitskalicky.lepsirozvrh.MainApplication;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhWrapper;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification;

import static cz.vitskalicky.lepsirozvrh.bakaAPI.ResponseCode.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class RozvrhTableFragment extends Fragment {
    public static final String TAG = RozvrhTableFragment.class.getSimpleName();
    public static final String TAG_TIMER = TAG + "-timer";

    private View view;
    private RozvrhLayout rozvrhLayout;
    private ScrollView scrollView;
    private LiveData<RozvrhWrapper> liveData;

    /**<code>false</code> when rozvrh for a certain week is displayed for the first time (a.k.a. <code>true</code> when being re-displayed
     * as fresh rozvrh is received from the internet)
     */
    private boolean redisplayed;
    private boolean scrollToCurrentLesson;

    private DisplayInfo displayInfo;

    private LocalDate week = null;
    private int weekIndex = 0; //what week is it from now (0: this, 1: next, -1: last, Integer.MAX_VALUE: permanent)
    private boolean cacheSuccessful = false;
    private boolean offline = false;
    private RozvrhAPI rozvrhAPI = null;
    private MainApplication mainApplication = null;


    public RozvrhTableFragment() {
        // Required empty public constructor
    }

    /**
     * must be called
     */
    public void init(RozvrhAPI rozvrhAPI, DisplayInfo displayInfo) {
        this.rozvrhAPI = rozvrhAPI;
        this.displayInfo = displayInfo;
        mainApplication = (MainApplication) getContext().getApplicationContext();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //debug timing: Log.d(TAG_TIMER, "onCreateView start " + Utils.getDebugTime());
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_rozvrh_table, container, false);

        rozvrhLayout = view.findViewById(R.id.rozvrhLayout);
        //scrollView = view.findViewById(R.id.horizontalScrollView);

        //debug timing: Log.d(TAG_TIMER, "onCreateView end " + Utils.getDebugTime());
        return view;
    }


    private int netCode = -1;

    /**
     * @param weekIndex index of week to display relative to now (0 = this week, 1 = next, -1 = previous) or {@code Integer.MAX_VALUE} for permanent
     */
    public void displayWeek(int weekIndex, boolean scrollToCurrentLesson) {
        //debug timing: Log.d(TAG_TIMER, "displayWeek start " + Utils.getDebugTime());

        this.weekIndex = weekIndex;
        if (weekIndex == Integer.MAX_VALUE)
            week = null;
        else
            week = Utils.getDisplayWeekMonday(getContext()).plusWeeks(weekIndex);

        final LocalDate finalWeek = week;
        redisplayed = false;
        this.scrollToCurrentLesson = scrollToCurrentLesson;

        displayInfo.setLoadingState(DisplayInfo.LOADING);
        cacheSuccessful = false;
        String infoMessage = Utils.getfl10nedWeekString(weekIndex, getContext());
        if (offline) {
            infoMessage += " (" + getString(R.string.info_offline) + ")";
        } else {
            displayInfo.setErrorMessage(null);
        }

        displayInfo.setMessage(infoMessage);

        if (liveData != null)
            liveData.removeObservers(this);
        liveData = rozvrhAPI.getLiveData(week);
        RozvrhWrapper rw = liveData.getValue();
        Rozvrh item = rw == null ? null : liveData.getValue().getRozvrh();
        if (item != null) {
            rozvrhLayout.setRozvrh(item, !redisplayed && scrollToCurrentLesson);
            redisplayed = true;
            if (rw.getSource() == RozvrhWrapper.SOURCE_MEMORY){
                if (offline) {
                    displayInfo.setLoadingState(DisplayInfo.ERROR);
                } else {
                    displayInfo.setLoadingState(DisplayInfo.LOADED);
                }
            }
        } else {
            rozvrhLayout.empty();
        }

        liveData.observe(this, rozvrhWrapper -> {
            if (rozvrhWrapper.getSource() == RozvrhWrapper.SOURCE_CACHE){
                onCacheResponse(rozvrhWrapper.getCode(), rozvrhWrapper.getRozvrh(), finalWeek);
            }else if (rozvrhWrapper.getSource() == RozvrhWrapper.SOURCE_NET){
                onNetResponse(rozvrhWrapper.getCode(), rozvrhWrapper.getRozvrh(), finalWeek);
            }
        });

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
            rozvrhLayout.setRozvrh(rozvrh, !redisplayed && scrollToCurrentLesson);
            redisplayed = true;
        }
        //onNetLoaded
        if (code == SUCCESS) {
            if (offline) {
                rozvrhAPI.clearMemory();
            }
            offline = false;
            displayInfo.setErrorMessage(null);
            displayInfo.setMessage(Utils.getfl10nedWeekString(weekIndex, getContext()));
            displayInfo.setLoadingState(DisplayInfo.LOADED);

            /*//if it is the current or next week, which may influence the notification, update time
            //TODO: this is very dirty, you must rewrite this when you finally get to rewrite RozvrhAPI
            if (week != null && rozvrh != null && (week.equals(Utils.getCurrentMonday()) || week.equals(Utils.getCurrentMonday().plusWeeks(1)))){
                final LocalDateTime current = mainApplication.getScheduledNotificationTime();
                rozvrhAPI.getNextNotificationUpdateTime(updateTime -> {
                    if (current == null){
                        if (updateTime != null){
                            mainApplication.scheduleNotificationUpdate(updateTime);
                        }
                    }else if (!current.equals(updateTime)){
                        mainApplication.scheduleNotificationUpdate(updateTime);
                    }
                });
            }
            //this should update the notification when the rozvrh changes
            //TODO: and it is dirty as well.
            if (week != null && rozvrh != null && week.equals(Utils.getDisplayWeekMonday(getContext()))){
                PermanentNotification.update(mainApplication, rozvrhAPI, () -> {});
            }*/
        } else {
            offline = true;
            displayInfo.setLoadingState(DisplayInfo.ERROR);

            String errorMessage = null;
            if (code == UNREACHABLE) {
                errorMessage = getString(R.string.info_unreachable);
            } else if (code == UNEXPECTED_RESPONSE) {
                errorMessage = getString(R.string.info_unexpected_response);
            } else if (code == LOGIN_FAILED) {
                errorMessage = getString(R.string.info_login_failed);
            }

            displayInfo.setErrorMessage(errorMessage);

            if (cacheSuccessful) {
                displayInfo.setMessage(Utils.getfl10nedWeekString(weekIndex, getContext()) + " (" + getString(R.string.info_offline) + ")");
            } else {
                displayInfo.setMessage(errorMessage);
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
            rozvrhLayout.setRozvrh(rozvrh, !redisplayed && scrollToCurrentLesson);
            redisplayed = true;
        }
    }

    public void refresh() {
        final LocalDate finalWeek = week;
        displayInfo.setLoadingState(DisplayInfo.LOADING);
        cacheSuccessful = false;

        rozvrhAPI.refresh(week, rw -> {
            /*if (rw.getCode() != SUCCESS){
                displayWeek(weekIndex, false);
            }else {*/
                onNetResponse(rw.getCode(), rw.getRozvrh(), finalWeek);
            /*}*/
        });
    }

    public void createViews(){
        rozvrhLayout.createViews();
    }
}

package cz.vitskalicky.lepsirozvrh.view;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.DisplayInfo;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.Utils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

/**
 * A simple {@link Fragment} subclass.
 */
public class RozvrhTableFragment extends Fragment {
    public static final String TAG = RozvrhTableFragment.class.getSimpleName();

    View view;
    TableLayout tableLayout;

    int rows = 0;
    int columns = 0;
    int spread = 1; //how many places should occupy default cell - there may be 2 lessons in one caption

    int cellWidth;

    CornerCell cornerCell;
    DenCell[] denCells = new DenCell[0];
    CaptionCell[] captionCells = new CaptionCell[0];
    List<List<HodinaCell>> hodinaCells = new ArrayList<>();

    TableRow captionRow;
    TableRow[] tableRows = new TableRow[0];

    DisplayInfo displayInfo;

    private LocalDate week = LocalDate.now();
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
    public void init(RozvrhAPI rozvrhAPI, DisplayInfo displayInfo){
        this.rozvrhAPI = rozvrhAPI;
        this.displayInfo = displayInfo;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_rozvrh_table, container, false);

        cellWidth = calculateCellWidth(container);

        tableLayout = view.findViewById(R.id.tableLayout);
        captionRow = new TableRow(getContext());
        cornerCell = new CornerCell(getContext(),captionRow, tableLayout, rows + 1, cellWidth);

        return view;
    }

    public void populate(Rozvrh rozvrh){
        //check if not detached
        if (getContext() == null){
            return;
        }

        int oldRows = rows;
        int oldColumns = columns;
        rows = rozvrh.getDny().size();
        columns = rozvrh.getHodiny().size();
        spread = calcucateSpread(rozvrh);

        RozvrhAPI.rememberRows(getContext(), rows);
        RozvrhAPI.rememberColumns(getContext(), columns);

        cornerCell.view.setRows(rows + 1);

        if (oldRows != rows || oldColumns != columns){
            createViews();
        }

        //populate
        cornerCell.update(rozvrh);
        for (int i = 0; i < columns; i++) {
            if (captionCells[i] == null) captionCells[i] = new CaptionCell(getContext(), captionRow, tableLayout, rows + 1, cellWidth);
            captionCells[i].update(rozvrh.getHodiny().get(i));
            captionCells[i].view.setSpread(spread);
        }

        for (int i = 0; i < rows; i++) {
            RozvrhDen den = rozvrh.getDny().get(i);
            if (denCells[i] == null) denCells[i] = new DenCell(getContext(), tableRows[i], tableLayout, rows + 1, cellWidth);
            denCells[i].update(den);

            String prevCaption = "";
            int captionsInRow = 1;
            int j = 0;
            for (; j < den.getHodiny().size(); j++) {
                RozvrhHodina item = den.getHodiny().get(j);

                //handling more lessons in same time (permanent timetable - different weeks)
                if (item.getCaption() != null && !item.getCaption().equals("") &&
                        item.getCaption().equals(prevCaption)){
                    // if there are more lessons in same time
                    captionsInRow++;
                }else {
                    if (captionsInRow > 1){ //if there were more lessons in the same time, update their weight with (default weight)/(number of lessons in the same time)
                        for (int k = 0; k < captionsInRow; k++) {
                            hodinaCells.get(i).get(j - (k + 1)).updateWeight(spread / (float) captionsInRow);
                        }
                    }
                    // reset captions in row
                    captionsInRow = 1;
                }

                prevCaption = item.getCaption();

                if (hodinaCells.get(i).size() <= j){
                    HodinaCell toAdd = new HodinaCell(getContext(), tableRows[i], tableLayout, rows + 1, cellWidth);
                    hodinaCells.get(i).add(toAdd);
                    tableRows[i].addView(toAdd.view);
                }

                hodinaCells.get(i).get(j).update(den.getHodiny().get(j),spread);
            }
            for (; j < columns; j++) {
                hodinaCells.get(i).get(j).update(null,spread);
            }
            //Remove cells that are left over from a multiple-cells-in-caption timetable
            final int lastIndex = j;
            final int size =  hodinaCells.get(i).size();
            for (; j < size; j++){
                HodinaCell toRemove = hodinaCells.get(i).get(lastIndex);
                hodinaCells.get(i).remove(toRemove);
                ViewGroup parent = (ViewGroup) toRemove.view.getParent();
                if (parent != null) {
                    parent.removeView(toRemove.view);
                }
            }
        }
    }

    /**
     * Creates a cell with reasonably long data and calculates its minimum width
     */
    private int calculateCellWidth(ViewGroup parent){
        RozvrhHodina hodina = new RozvrhHodina();
        hodina.setZkrpr("MMmM");
        hodina.setZkruc("Mmmm");
        hodina.setZkruc("M. 00");
        hodina.setZkrskup("MMMm 0");
        hodina.setCycle("XXXX");
        HodinaCell hodinaCell = new HodinaCell(getContext(), hodina, 1, parent, parent, 1, 1);
        CellView cellView = hodinaCell.view;

        int minWidth = cellView.getNaturalWidth();
        return minWidth;
    }

    public void createViews(){
        if (rows == 0 && columns == 0){
            rows = RozvrhAPI.getRememberedRows(getContext());
            columns = RozvrhAPI.getRememberedColumns(getContext());
        }

        denCells = new DenCell[rows];
        captionCells = new CaptionCell[columns];
        hodinaCells = new ArrayList<>();
        tableRows = new TableRow[rows];

        for (int i = 0; i < rows; i++) {
            TableRow item = new TableRow(getContext());
            item.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1));
            tableRows[i] = item;
        }

        for (int i = 0; i < columns; i++) {
            captionCells[i] = new CaptionCell(getContext(), captionRow, tableLayout, rows + 1, cellWidth);
        }

        for (int i = 0; i < rows; i++) {
            denCells[i] = new DenCell(getContext(), tableRows[i], tableLayout, rows + 1, cellWidth);

            List<HodinaCell> newList = new ArrayList<>();

            for (int j = 0; j < columns; j++) {
                newList.add(new HodinaCell(getContext(), tableRows[i], tableLayout, rows + 1, cellWidth));
            }

            hodinaCells.add(newList);
        }

        fillViews();
    }


    private int calcucateSpread(Rozvrh rozvrh){
        int mostSpread = 1;
        for (int i = 0; i < rozvrh.getDny().size(); i++) {
            RozvrhDen item = rozvrh.getDny().get(i);

            String lastCaption = "";
            int captionsInRow = 1;
            for (int j = 0; j < item.getHodiny().size(); j++) {
                RozvrhHodina item2 = item.getHodiny().get(j);

                if (item2.getCaption() == null || item2.getCaption().equals("")) {
                    captionsInRow = 1;
                } else if (item2.getCaption().equals(lastCaption)){
                    captionsInRow++;
                    mostSpread = Math.max(mostSpread, captionsInRow);
                }else {
                    captionsInRow = 1;
                }
                lastCaption = item2.getCaption();
            }
        }
        return mostSpread;
    }

    private void fillViews(){
        captionRow.removeAllViews();
        for (int i = 0; i < tableRows.length; i++) {
            tableRows[i].removeAllViews();
        }
        tableLayout.removeAllViews();

        captionRow.addView(cornerCell.getView());
        for (int i = 0; i < columns; i++) {
            captionRow.addView(captionCells[i].getView());
        }
        tableLayout.addView(captionRow);
        for (int i = 0; i < rows; i++) {
            TableRow tr = tableRows[i];
            tr.addView(denCells[i].getView());
            for (int j = 0; j < hodinaCells.get(i).size(); j++) {
                tr.addView(hodinaCells.get(i).get(j).view);
            }
            tableLayout.addView(tableRows[i]);
        }
    }

    /**
     * Empty the table when loading to prevent confusion
     */
    private void empty(){
        //check if fragment was not removed while loading
        if (getContext() == null){
            return;
        }
        final int oldRows = rows;
        final int oldColumns = columns;
        rows = RozvrhAPI.getRememberedRows(getContext());
        columns = RozvrhAPI.getRememberedColumns(getContext());
        spread = 1;

        cornerCell.view.setRows(rows + 1);

        if (oldRows != rows || oldColumns != columns){
            createViews();
        }

        //populate
        cornerCell.empty();
        for (int i = 0; i < columns; i++) {
            if (captionCells[i] == null) captionCells[i] = new CaptionCell(getContext(), captionRow, tableLayout, rows + 1, cellWidth);
            captionCells[i].empty();
            captionCells[i].view.setSpread(spread);
        }

        for (int i = 0; i < rows; i++) {
            if (denCells[i] == null) denCells[i] = new DenCell(getContext(), tableRows[i], tableLayout, rows + 1, cellWidth);
            denCells[i].empty();

            int j = 0;
            for (; j < columns; j++) {
                hodinaCells.get(i).get(j).empty();
            }
            //Remove cells that are left over from a multiple-cells-in-caption timetable
            final int lastIndex = j;
            final int size =  hodinaCells.get(i).size();
            for (; j < size; j++){
                HodinaCell toRemove = hodinaCells.get(i).get(lastIndex);
                hodinaCells.get(i).remove(toRemove);
                ViewGroup parent = (ViewGroup) toRemove.view.getParent();
                if (parent != null) {
                    parent.removeView(toRemove.view);
                }
            }
        }
    }


    private int netCode = -1;

    /**
     *
     * @param weekIndex index of week to display relative to now (0 = this week, 1 = next, -1 = previous) or {@code Integer.MAX_VALUE} for permanent
     */
    public void displayWeek(int weekIndex){
        this.weekIndex = weekIndex;
        if (weekIndex == Integer.MAX_VALUE)
            week = null;
        else
            week = Utils.getWeekMonday(LocalDate.now().plusWeeks(weekIndex));

        final LocalDate finalWeek = week;

        displayInfo.setLoadingState(DisplayInfo.LOADING);
        cacheSuccessful = false;
        displayInfo.setMessage(Utils.getfl10nedWeekString(weekIndex, getContext()));
        if (offline)
            displayInfo.setMessage(displayInfo.getMessage() + " (" + getString(R.string.info_offline) + ")");

        netCode = -1;
        Rozvrh item = rozvrhAPI.get(week, (code, rozvrh) -> {
            //onCachLoaded
            // have to make sure that net was not faster
            if (netCode != RozvrhAPI.SUCCESS)
                onCacheResponse(code, rozvrh, finalWeek);
            if (netCode != -1 && netCode != RozvrhAPI.SUCCESS){
                onNetResponse(netCode, null, finalWeek);
            }
        },(code, rozvrh) -> {
            netCode = code;
            onNetResponse(code, rozvrh, finalWeek);
        });
        if (item != null){
            populate(item);
            displayInfo.setLoadingState(DisplayInfo.LOADED);
        }else {
            empty();
        }
    }

    private void onNetResponse(int code, Rozvrh rozvrh, final LocalDate finalWeek){
        //check if fragment was not removed while loading
        if (getContext() == null){
            return;
        }
        if (week != finalWeek){
            return;
        }
        //onNetLoaded
        if (code == RozvrhAPI.SUCCESS){
            populate(rozvrh);
            if (offline){
                rozvrhAPI.clearMemory();
            }
            offline = false;
            displayInfo.setMessage(Utils.getfl10nedWeekString(weekIndex, getContext()));
            displayInfo.setLoadingState(DisplayInfo.LOADED);
        } else {
            offline = true;
            displayInfo.setLoadingState(DisplayInfo.ERROR);
            if (cacheSuccessful){
                displayInfo.setMessage(Utils.getfl10nedWeekString(weekIndex, getContext()) + " (" + getString(R.string.info_offline) + ")");
            }else if (code == RozvrhAPI.UNREACHABLE) {
                displayInfo.setMessage(getString(R.string.info_unreachable));
            } else if (code == RozvrhAPI.UNEXPECTED_RESPONSE){
                displayInfo.setMessage(getString(R.string.info_unexpected_response));
            }else if (code == RozvrhAPI.LOGIN_FAILED){
                displayInfo.setMessage(getString(R.string.info_login_failed));
            }
        }
    }

    private void onCacheResponse(int code, Rozvrh rozvrh, final LocalDate finalWeek){
        //check if fragment was not removed while loading
        if (getContext() == null){
            return;
        }
        if (week != finalWeek){
            return;
        }
        if (code == RozvrhAPI.SUCCESS){
            cacheSuccessful = true;
            populate(rozvrh);
        }
    }

    public void refresh(){
        final LocalDate finalWeek = week;
        displayInfo.setLoadingState(DisplayInfo.LOADING);

        rozvrhAPI.refresh(week, (code, rozvrh) -> {
            //check if fragment was not removed while loading
            if (getContext() == null){
                return;
            }
            if (week != finalWeek)
                return;
            if (rozvrh != null)
                populate(rozvrh);
            if (code == RozvrhAPI.SUCCESS){
                displayInfo.setLoadingState(DisplayInfo.LOADED);
                displayInfo.setMessage((Utils.getfl10nedWeekString(weekIndex, getContext())));
            }else {
                displayInfo.setLoadingState(DisplayInfo.ERROR);
                if (code == RozvrhAPI.UNREACHABLE) {
                    displayInfo.setMessage(getString(R.string.info_unreachable));
                } else if (code == RozvrhAPI.UNEXPECTED_RESPONSE){
                    displayInfo.setMessage(getString(R.string.info_unexpected_response));
                }else if (code == RozvrhAPI.LOGIN_FAILED){
                    displayInfo.setMessage(getString(R.string.info_login_failed));
                }
            }
        });
    }

}

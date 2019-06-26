package cz.vitskalicky.lepsirozvrh.view;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.android.volley.toolbox.Volley;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

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

    View view;
    TableLayout tableLayout;

    int rows = 0;
    int columns = 0;
    int spread = 1; //how many places should occupy default cell - there may be 2 lessons in one caption

    CornerCell cornerCell;
    DenCell[] denCells = new DenCell[0];
    CaptionCell[] captionCells = new CaptionCell[0];
    List<List<HodinaCell>> hodinaCells = new ArrayList<>();

    TableRow captionRow;
    TableRow[] tableRows = new TableRow[0];

    public RozvrhTableFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_rozvrh_table, container, false);

        tableLayout = view.findViewById(R.id.tableLayout);
        captionRow = new TableRow(getContext());
        cornerCell = new CornerCell(getContext(),captionRow);

        //<debug>
        RozvrhAPI.getRozvrh(null, Volley.newRequestQueue(getContext()), getContext(),(code, rozvrh) -> {
            //on cache
            if (code == RozvrhAPI.SUCCESS){
                System.out.println("Cache: Zdarilo se");
                populate(rozvrh);
            }else {
                System.out.println("Cache: Nezdarilo se: " + code);
            }
        },(code, rozvrh) -> {
            //on net
            if (code == RozvrhAPI.SUCCESS){
                System.out.println("Net: zdarilo se");
                populate(rozvrh);
            }else {
                System.out.println("Net: Nezdarilo se: " + code);
            }
        });
        //</debug>

        return view;
    }

    public void populate(Rozvrh rozvrh){
        int oldRows = rows;
        int oldColumns = columns;
        rows = rozvrh.getDny().size();
        columns = rozvrh.getHodiny().size();
        spread = calcucateSpread(rozvrh);

        if (oldRows != rows || oldColumns != columns){
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
                captionCells[i] = new CaptionCell(getContext(), captionRow);
            }

            for (int i = 0; i < rows; i++) {
                denCells[i] = new DenCell(getContext(), tableRows[i]);

                List<HodinaCell> newList = new ArrayList<>();

                for (int j = 0; j < columns; j++) {
                    newList.add(new HodinaCell(getContext(), tableRows[i]));
                }

                hodinaCells.add(newList);
            }

            fillViews();
        }

        //populate
        cornerCell.update(rozvrh);
        for (int i = 0; i < columns; i++) {
            if (captionCells[i] == null) captionCells[i] = new CaptionCell(getContext(), captionRow);
            captionCells[i].update(rozvrh.getHodiny().get(i));
        }

        for (int i = 0; i < rows; i++) {
            RozvrhDen den = rozvrh.getDny().get(i);
            if (denCells[i] == null) denCells[i] = new DenCell(getContext(), tableRows[i]);
            denCells[i].update(den);

            String prevCaption = "";
            int captionsInRow = 1;
            for (int j = 0; j < den.getHodiny().size(); j++) {
                RozvrhHodina item = den.getHodiny().get(j);

                //handling more lessons in same time (permanent timetable - different weeks)
                if (!(item.getCaption() == null || item.getCaption().equals("")) &&
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

                if (hodinaCells.get(i).size() <= j){
                    hodinaCells.get(i).add(new HodinaCell(getContext(), tableRows[i]));
                }

                hodinaCells.get(i).get(j).update(den.getHodiny().get(j),spread);
            }
        }
    }

    private int calcucateSpread(Rozvrh rozvrh){
        int mostSpread = 1;
        for (int i = 0; i < rozvrh.getDny().size(); i++) {
            RozvrhDen item = rozvrh.getDny().get(i);

            String lastCaption = "";
            int captionsInRow = 1;
            for (int j = 0; j < item.getHodiny().size(); j++) {
                RozvrhHodina item2 = item.getHodiny().get(i);

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

}

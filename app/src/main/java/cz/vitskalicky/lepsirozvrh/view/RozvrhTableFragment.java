package cz.vitskalicky.lepsirozvrh.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;

/**
 * A simple {@link Fragment} subclass.
 */
public class RozvrhTableFragment extends Fragment {

    View view;
    TableLayout tableLayout;

    int rows = 0;
    int columns = 0;

    CornerCell cornerCell;
    DenCell[] denCells = new DenCell[0];
    CaptionCell[] captionCells = new CaptionCell[0];
    HodinaCell[][] hodinaCells = new HodinaCell[0][];

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

        return view;
    }

    public void populate(Rozvrh rozvrh){
        int oldRows = rows;
        int oldColumns = columns;
        rows = rozvrh.getDny().size();
        columns = rozvrh.getHodiny().size();

        if (oldRows != rows || oldColumns != columns){
            denCells = new DenCell[rows];
            captionCells = new CaptionCell[columns];
            hodinaCells = new HodinaCell[rows][columns];
            tableRows = new TableRow[rows];

            for (int i = 0; i < rows; i++) {
                tableRows[i] = new TableRow(getContext());
            }

            for (int i = 0; i < columns; i++) {
                captionCells[i] = new CaptionCell(getContext(), captionRow);
            }

            for (int i = 0; i < rows; i++) {
                denCells[i] = new DenCell(getContext(), tableRows[i]);

                for (int j = 0; j < columns; j++) {
                    hodinaCells[i][j] = new HodinaCell(getContext(), tableRows[i]);
                }
            }

            fillViews();
        }

        //populate
        cornerCell.update(rozvrh);
        for (int i = 0; i < columns; i++) {
            captionCells[i].update(rozvrh.getHodiny().get(i));
        }

        for (int i = 0; i < rows; i++) {
            RozvrhDen den = rozvrh.getDny().get(i);
            denCells[i].update(den);
            for (int j = 0; j < den.getHodiny().size(); j++) {
                hodinaCells[i][j].update(den.getHodiny().get(i));
            }
        }
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
            for (int j = 0; j < hodinaCells[i].length; j++) {
                tr.addView(hodinaCells[i][j].view);
            }
            tableLayout.addView(tableRows[i]);
        }
    }

}

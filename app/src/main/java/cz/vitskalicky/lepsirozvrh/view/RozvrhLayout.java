package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;

public class RozvrhLayout extends ViewGroup {
    private int naturalCellWidth = -1;

    private Context context;
    private Rozvrh rozvrh;

    private int rows = 0; //only actual lessons - add 1 to calculate with captions as well
    private int columns = 0; //only actual lessons - add 1 to calculate with day cells as well
    private int spread = 1;

    private int childWidth;
    private int childHeight;

    private CornerCell cornerCell;
    private DenCell[] denCells = new DenCell[0];
    private CaptionCell[] captionCells = new CaptionCell[0];
    private List<List<HodinaCell>> hodinaCells = new ArrayList<>();

    private HodinaCell nextHodinaCell = null; //the highlighted one
    private HodinaCell nextHodinaCellRight = null; //the right one from the highlighted one (it has its left highlighted)
    private HodinaCell nextHodinaCellBottom = null; //the bottom one from the highlighted one (it has its top highlighted)
    private HodinaCell nextHodinaCellCorner = null; //the corner one from the highlighted one (it has its corner highlighted)

    public RozvrhLayout(Context context) {
        super(context);
        this.context = context;
    }

    public RozvrhLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = Math.max(MeasureSpec.getSize(widthMeasureSpec), getSuggestedMinimumWidth()) + 1;
        int height = Math.max(MeasureSpec.getSize(heightMeasureSpec), getSuggestedMinimumHeight());
        int childState = 0;


        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED){
            int naturalCellWidth =  getNaturalCellWidth();
            childWidth = naturalCellWidth;
            width = naturalCellWidth * spread * columns + naturalCellWidth;
        }else {
            childWidth = width / ((columns * spread) + 1);
        }
        if (rows == 0 || columns == 0){
            setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                    resolveSizeAndState(height, heightMeasureSpec,
                            childState << MEASURED_HEIGHT_STATE_SHIFT));
            return;
        }

        childHeight = (int)Math.ceil((double)height / (rows + 1));

        int dayWidthMS = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        int childHeightMS = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);

        if (cornerCell != null){
            measureChild(cornerCell.getView(), dayWidthMS, childHeightMS);
            childState = combineMeasuredStates(childState, cornerCell.view.getMeasuredState());
        }
        for (DenCell item : denCells) {
            measureChild(item.getView(), dayWidthMS, childHeightMS);
            childState = combineMeasuredStates(childState, item.view.getMeasuredState());
        }
        for (CaptionCell item : captionCells) {
            int hodinaWidthMS = MeasureSpec.makeMeasureSpec(childWidth * spread, MeasureSpec.EXACTLY);
            measureChild(item.getView(), hodinaWidthMS, childHeightMS);
            childState = combineMeasuredStates(childState, item.view.getMeasuredState());
        }
        for (List<HodinaCell> list: hodinaCells){
            for (HodinaCell item :list) {
                int hodinaWidthMS = MeasureSpec.makeMeasureSpec((int)(childWidth * item.spread), MeasureSpec.EXACTLY);
                measureChild(item.getView(), hodinaWidthMS, childHeightMS);
                childState = combineMeasuredStates(childState, item.view.getMeasuredState());
            }
        }

        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(height, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (rows == 0 || columns == 0) {
            return;
        }

        if (cornerCell != null){
            cornerCell.getView().layout(l, t,l + childWidth, t + childHeight);
        }
        for (int i = 0; i < denCells.length; i++) {
            denCells[i].getView().layout(l,t + ((i + 1)*childHeight), l + childWidth,t + ((i + 2)*childHeight));
        }
        for (int i = 0; i < captionCells.length; i++) {
            captionCells[i].getView().layout(l + childWidth + (i * childWidth * spread),t, l + childWidth + ((i + 1) * childWidth * spread),t + childHeight);
        }
        for (int y = 0; y < hodinaCells.size(); y++) {
            float n = 0;
            for (int x = 0; x < hodinaCells.get(y).size(); x++) {
                HodinaCell item = hodinaCells.get(y).get(x);
                float itemSpread = item.spread;
                item.getView().layout(l + (int)((n + 1) * childWidth), t + ((y + 1) * childHeight), l + (int)((n + 1 + itemSpread) * childWidth), t + ((y + 2) * childHeight));
                n += itemSpread;
            }
        }
    }

    /**
     * Creates a cell with reasonably long data and calculates its minimum width
     */
    private int getNaturalCellWidth() {
        if (naturalCellWidth != -1){
            return naturalCellWidth;
        }
        RozvrhHodina hodina = new RozvrhHodina();
        hodina.setZkrpr("MMmM");
        hodina.setZkruc("Mmmm");
        hodina.setZkrmist("M. 00");
        hodina.setZkrskup("MMMMm 0");
        hodina.setCycle("XXXX");
        HodinaCell hodinaCell = new HodinaCell(context, hodina, this, true);
        View view = hodinaCell.view;

        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int wMSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int hMSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(wMSpec, hMSpec);

        int minWidth = view.getMeasuredWidth();
        naturalCellWidth = minWidth;
        return minWidth;
    }

    public void createViews() {
        //debug timing: Log.d(TAG_TIMER, "createViews start " + Utils.getDebugTime());
        if (rows == 0 && columns == 0) {
            rows = RozvrhAPI.getRememberedRows(context);
            columns = RozvrhAPI.getRememberedColumns(context);
        }

        if (denCells.length == rows && captionCells.length == columns && hodinaCells.size() == rows) {
            //debug timing: Log.d(TAG_TIMER, "createViews end " + Utils.getDebugTime());
            return;
        }

        removeAllViews();

        denCells = new DenCell[rows];
        captionCells = new CaptionCell[columns];
        hodinaCells = new ArrayList<>();

        if (cornerCell == null){
            cornerCell = new CornerCell(context, this);
        }
        addView(cornerCell.getView());


        for (int i = 0; i < columns; i++) {
            CaptionCell item = new CaptionCell(context,this);
            captionCells[i] = item;
            addView(item.getView());
        }

        for (int i = 0; i < rows; i++) {
            DenCell denCell = new DenCell(context, this);
            denCells[i] = denCell;
            addView(denCell.getView());

            List<HodinaCell> newList = new ArrayList<>();

            for (int j = 0; j < columns; j++) {
                HodinaCell item = new HodinaCell(context, this);
                newList.add(item);
                addView(item.getView());
            }

            hodinaCells.add(newList);
        }

        //debug timing: Log.d(TAG_TIMER, "createViews end " + Utils.getDebugTime());
    }

    public void setRozvrh(Rozvrh rozvrh) {
        //debug timing: Log.d(TAG_TIMER, "populate start " + Utils.getDebugTime());
        this.rozvrh = rozvrh;
        int oldRows = rows;
        int oldColumns = columns;
        rows = rozvrh.getDny().size();
        columns = rozvrh.getHodiny().size();
        spread = calcucateSpread(rozvrh);
        boolean perm = rozvrh.getTyp().equals("perm");

        RozvrhAPI.rememberRows(context, rows);
        RozvrhAPI.rememberColumns(context, columns);

        if (oldRows != rows || oldColumns != columns) {
            createViews();
        }

        //populate
        cornerCell.update(rozvrh);
        for (int i = 0; i < columns; i++) {
            CaptionCell item = captionCells[i];
            if (item == null){
                item = new CaptionCell(context, this);
                captionCells[i] = item;
                addView(item.getView());
            }            
            item.update(rozvrh.getHodiny().get(i));
        }

        for (int i = 0; i < rows; i++) {
            RozvrhDen den = rozvrh.getDny().get(i);
            DenCell denCell = denCells[i];
            if (denCell == null){
                denCell = new DenCell(context, this);
                denCells[i] = denCell;
                addView(denCell.getView());
            }
            denCells[i].update(den);

            int cellsOverBySpread = 0; //how many more cells than captions are there due to more cell in one caption

            String prevCaption = "";
            int captionsInRow = 1;
            int j = 0;
            for (; j < den.getHodiny().size(); j++) {
                RozvrhHodina item = den.getHodiny().get(j);

                if (hodinaCells.get(i).size() <= j) {
                    HodinaCell toAdd = new HodinaCell(context, this);
                    hodinaCells.get(i).add(toAdd);
                    addView(toAdd.view);
                }
                //handling more lessons in same time (permanent timetable - different weeks)
                if (item.getCaption() != null && !item.getCaption().equals("") &&
                        item.getCaption().equals(prevCaption)) {
                    // if there are more lessons in same time
                    captionsInRow++;
                } else {
                    if (captionsInRow > 1){//if there were more lessons in the same time, update their weight with (default weight)/(number of lessons in the same time)
                        for (int k = 0; k < captionsInRow; k++) {
                            hodinaCells.get(i).get(j - (k + 1)).updateSpread(spread / (float) captionsInRow);
                        }
                    }
                    cellsOverBySpread += captionsInRow - 1;
                    // reset captions in row
                    captionsInRow = 1;
                }

                prevCaption = item.getCaption();

                hodinaCells.get(i).get(j).update(item, perm);
                hodinaCells.get(i).get(j).updateSpread(spread);
            }
            for (; j < columns + cellsOverBySpread; j++) {
                if (hodinaCells.get(i).size() <= j) {
                    HodinaCell toAdd = new HodinaCell(context, this);
                    hodinaCells.get(i).add(toAdd);
                    addView(toAdd.view);
                }
                hodinaCells.get(i).get(j).update(null, perm);
                hodinaCells.get(i).get(j).updateSpread(spread);
            }
            //Remove cells that are left over from a multiple-cells-in-caption timetable
            final int lastIndex = j;
            final int size = hodinaCells.get(i).size();
            for (; j < size; j++) {
                HodinaCell toRemove = hodinaCells.get(i).get(lastIndex);
                hodinaCells.get(i).remove(toRemove);
                removeView(toRemove.view);
            }
        }

        highlightCurrentLesson();

        //debug timing: Log.d(TAG_TIMER, "populate end " + Utils.getDebugTime());
    }

    public void highlightCurrentLesson() {
        if (rozvrh == null)
            return;
        Rozvrh.GetNLreturnValues values = rozvrh.getNextLesson();


        //unhighlight
        if (nextHodinaCell != null) {
            nextHodinaCell.hightlightEdges(false, false, false);
            nextHodinaCell.highlightItself(false);
        }
        if (nextHodinaCellRight != null) {
            nextHodinaCellRight.hightlightEdges(false, false, false);
        }
        if (nextHodinaCellBottom != null) {
            nextHodinaCellBottom.hightlightEdges(false, false, false);
        }
        if (nextHodinaCellCorner != null) {
            nextHodinaCellCorner.hightlightEdges(false, false, false);
        }


        if (values == null || values.rozvrhHodina == null) {
            return;
        }

        RozvrhHodina hodina = values.rozvrhHodina;
        int denIndex = values.dayIndex;
        int hodinaIndex = values.lessonIndex;

        nextHodinaCell = hodinaCells.get(denIndex).get(hodinaIndex);
        nextHodinaCell.hightlightEdges(true, true, true);
        nextHodinaCell.highlightItself(true);

        if (denIndex + 1 < hodinaCells.size()) {
            nextHodinaCellBottom = hodinaCells.get(denIndex + 1).get(hodinaIndex);
            nextHodinaCellBottom.hightlightEdges(true, false, true);
        }
        if (hodinaIndex + 1 < hodinaCells.get(0).size()) {
            nextHodinaCellRight = hodinaCells.get(denIndex).get(hodinaIndex + 1);
            nextHodinaCellRight.hightlightEdges(false, true, true);
        }
        if (denIndex + 1 < hodinaCells.size() && hodinaIndex + 1 < hodinaCells.get(0).size()) {
            nextHodinaCellCorner = hodinaCells.get(denIndex + 1).get(hodinaIndex + 1);
            nextHodinaCellCorner.hightlightEdges(false, false, true);
        }
    }

    private static int calcucateSpread(Rozvrh rozvrh) {
        //debug timing: Log.d(TAG_TIMER, "calculateSpread start " + Utils.getDebugTime());
        int mostSpread = 1;
        for (int i = 0; i < rozvrh.getDny().size(); i++) {
            RozvrhDen item = rozvrh.getDny().get(i);

            String lastCaption = "";
            int captionsInRow = 1;
            for (int j = 0; j < item.getHodiny().size(); j++) {
                RozvrhHodina item2 = item.getHodiny().get(j);

                if (item2.getCaption() == null || item2.getCaption().equals("")) {
                    captionsInRow = 1;
                } else if (item2.getCaption().equals(lastCaption)) {
                    captionsInRow++;
                    mostSpread = Math.max(mostSpread, captionsInRow);
                } else {
                    captionsInRow = 1;
                }
                lastCaption = item2.getCaption();
            }
        }
        //debug timing: Log.d(TAG_TIMER, "calculateSpread end " + Utils.getDebugTime());
        return mostSpread;
    }
    
    /**
            * Empty the table when loading to prevent confusion
     */
    public void empty() {
        //check if fragment was not removed while loading
        this.rozvrh = null;
        final int oldRows = rows;
        final int oldColumns = columns;
        rows = RozvrhAPI.getRememberedRows(getContext());
        columns = RozvrhAPI.getRememberedColumns(getContext());
        spread = 1;
        
        if (oldRows != rows || oldColumns != columns) {
            createViews();
        }

        //populate
        cornerCell.empty();
        for (int i = 0; i < columns; i++) {
            if (captionCells[i] == null)
                captionCells[i] = new CaptionCell(getContext(), this);
            captionCells[i].empty();
        }

        for (int i = 0; i < rows; i++) {
            if (denCells[i] == null)
                denCells[i] = new DenCell(getContext(), this);
            denCells[i].empty();

            int j = 0;
            for (; j < columns; j++) {
                hodinaCells.get(i).get(j).empty();
            }
            //Remove cells that are left over from a multiple-cells-in-caption timetable
            final int lastIndex = j;
            final int size = hodinaCells.get(i).size();
            for (; j < size; j++) {
                HodinaCell toRemove = hodinaCells.get(i).get(lastIndex);
                hodinaCells.get(i).remove(toRemove);
                removeView(toRemove.view);
            }
        }
    }
}

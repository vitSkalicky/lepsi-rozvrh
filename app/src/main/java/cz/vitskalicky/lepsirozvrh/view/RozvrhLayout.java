package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;
import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;

public class RozvrhLayout extends ViewGroup {
    public static final String TAG = RozvrhLayout.class.getSimpleName();

    private int naturalCellWidth = -1;
    private int childHeightWhenCalculatingNaturalCellWidth = -1;

    private Context context;
    private Rozvrh rozvrh;
    private boolean perm;

    private int rows = 0; //only actual lessons - add 1 to calculate with captions as well
    private int columns = 0; //only actual lessons - add 1 to calculate with day cells as well

    private int childHeight;

    private CornerView cornerView;
    private DenView[] denViews = new DenView[0];
    private CaptionView[] captionViews = new CaptionView[0];

    private HodinaView nextHodinaView = null; //the highlighted one
    private HodinaView nextHodinaViewRight = null; //the right one from the highlighted one (it has its left highlighted)
    private HodinaView nextHodinaViewBottom = null; //the bottom one from the highlighted one (it has its top highlighted)
    private HodinaView nextHodinaViewCorner = null; //the corner one from the highlighted one (it has its corner highlighted)

    private List<HodinaView>[][] hodinasByCaptions = new ArrayList[0][]; //the first paramemter is caption index, second day and the list contains all lessons in that "space"

    private HodinaViewRecycler hodinaViewRecycler;

    private int[] columnSizes = new int[0]; // includes days column

    public RozvrhLayout(Context context) {
        super(context);
        this.context = context;
        hodinaViewRecycler = new HodinaViewRecycler(context);
        //empty();
    }

    public RozvrhLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        hodinaViewRecycler = new HodinaViewRecycler(context);
        //empty();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int specWS = MeasureSpec.getSize(widthMeasureSpec);
        final int specWM = MeasureSpec.getMode(widthMeasureSpec);
        final int specHS = MeasureSpec.getSize(heightMeasureSpec);
        final int specHM = MeasureSpec.getMode(heightMeasureSpec);

        int width = specWS;
        int height = specHS;

        int childState = 0;

        childHeight = (int) Math.ceil((double) specHS / (rows + 1));
        int naturalCellWidth = getNaturalCellWidth();

        //calculate width of every column
        columnSizes[0] = naturalCellWidth;
        for (int i = 0; i < denViews.length; i++) {
            columnSizes[0] = Math.max(columnSizes[0], denViews[i].getMinimumWidth());
        }
        for (int i = 1; i < columnSizes.length; i++) {
            columnSizes[i] = Math.max(naturalCellWidth, captionViews[i - 1].getMinimumWidth());
            for (int j = 0; j < rows; j++) {
                int max = 0;
                int count = 0;
                for (HodinaView item : hodinasByCaptions[i-1][j]) {
                    max = Math.max(max, item.getMinimumWidth());
                    count++;
                }
                columnSizes[i] = Math.max(columnSizes[i], max * count);
            }
        }

        int prefferedWidth = 0;
        for (int columnSize : columnSizes) {
            prefferedWidth += columnSize;
        }

        if (specWM == MeasureSpec.UNSPECIFIED || (specWM == MeasureSpec.AT_MOST && prefferedWidth <= specWS)){
            width = prefferedWidth;
        }else {
            float widthRatio = specWS / (float)(prefferedWidth);
            for (int i = 0; i < columnSizes.length; i++) {
                columnSizes[i] = (int) Math.floor(columnSizes[i] * widthRatio);
            }
            width = specWS;
        }
        if (rows == 0 || columns == 0) {
            setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                    resolveSizeAndState(specHS, heightMeasureSpec,
                            childState << MEASURED_HEIGHT_STATE_SHIFT));
            return;
        }

        int childHeightMS = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);

        if (cornerView != null) {
            measureChild(cornerView, MeasureSpec.makeMeasureSpec(columnSizes[0], MeasureSpec.EXACTLY), childHeightMS);
            childState = combineMeasuredStates(childState, cornerView.getMeasuredState());
        }
        for (DenView item : denViews) {
            measureChild(item, MeasureSpec.makeMeasureSpec(columnSizes[0], MeasureSpec.EXACTLY), childHeightMS);
            childState = combineMeasuredStates(childState, item.getMeasuredState());
        }
        for (int i = 0; i < captionViews.length; i++) {
            CaptionView item = captionViews[i];
            int hodinaWidthMS = MeasureSpec.makeMeasureSpec(columnSizes[i + 1], MeasureSpec.EXACTLY);
            measureChild(item, hodinaWidthMS, childHeightMS);
            childState = combineMeasuredStates(childState, item.getMeasuredState());
        }
        for (int i = 0; i < hodinasByCaptions.length; i++) {
            for (int j = 0; j < hodinasByCaptions[i].length; j++) {
                for (HodinaView item: hodinasByCaptions[i][j]) {
                    int hodinaWidthMS = MeasureSpec.makeMeasureSpec((int) (columnSizes[i] / hodinasByCaptions[i][j].size()), MeasureSpec.EXACTLY);
                    measureChild(item, hodinaWidthMS, childHeightMS);
                    childState = combineMeasuredStates(childState, item.getMeasuredState());
                }
            }
        }

        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(specHS, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (rows == 0 || columns == 0) {
            return;
        }

        if (cornerView != null) {
            cornerView.layout(l, t, l + columnSizes[0], t + childHeight);
        }
        for (int i = 0; i < denViews.length; i++) {
            denViews[i].layout(l, t + ((i + 1) * childHeight), l + columnSizes[0], t + ((i + 2) * childHeight));
        }
        int prevColumnEnd = l + columnSizes[0];
        for (int i = 0; i < captionViews.length; i++) {
            int thisColumnEnd = prevColumnEnd + columnSizes[i + 1];
            if (i == columns - 1){
                //the last one
                captionViews[i].layout(prevColumnEnd, t, r, t + childHeight);
            }else {
                captionViews[i].layout(prevColumnEnd, t, thisColumnEnd, t + childHeight);
            }
            prevColumnEnd = thisColumnEnd;
        }
        prevColumnEnd = l + columnSizes[0];
        for (int i = 0; i < columns; i++) {
            int thisColumnEnd = prevColumnEnd + columnSizes[i + 1];
            for (int j = 0; j < rows; j++) {
                int cellWidth;
                if (hodinasByCaptions[i][j].size() > 0) {
                    cellWidth = columnSizes[i + 1] / hodinasByCaptions[i][j].size();
                }else {
                    cellWidth = columnSizes[i + 1];
                }
                int lastCellEnd = prevColumnEnd;
                List<HodinaView> views = hodinasByCaptions[i][j];
                for (int k = 0; k < views.size(); k++) {
                    HodinaView item = views.get(k);
                    if (i == columns - 1){
                        //the last one
                        item.layout(lastCellEnd, t + childHeight + (j * childHeight), r, t + childHeight + (j + 1) * childHeight);
                    }else {
                        item.layout(lastCellEnd, t + childHeight + (j * childHeight), lastCellEnd + cellWidth, t + childHeight + (j + 1) * childHeight);
                    }
                    lastCellEnd = lastCellEnd + cellWidth;
                }
            }
            prevColumnEnd = thisColumnEnd;
        }
    }

    /**
     * Creates a cell with reasonably long data and calculates its minimum width
     */
    private int getNaturalCellWidth() {
        if (naturalCellWidth != -1 && childHeightWhenCalculatingNaturalCellWidth == childHeight) {
            return naturalCellWidth;
        }

        HodinaView view = new HodinaView(context, null);

        int minWidth = Math.max(view.measureExampleWidth(), CellView.goldenRectangle(childHeight));
        naturalCellWidth = minWidth;
        childHeightWhenCalculatingNaturalCellWidth = childHeight;
        return minWidth;
    }

    public void createViews() {
        //debug timing: Log.d(TAG_TIMER, "createViews start " + Utils.getDebugTime());
        if (rows == 0 && columns == 0) {
            rows = RozvrhAPI.getRememberedRows(context);
            columns = RozvrhAPI.getRememberedColumns(context);
        }

        for (int i = 0; i < hodinasByCaptions.length; i++) {
            for (int j = 0; j < hodinasByCaptions[i].length; j++) {
                for (HodinaView item :hodinasByCaptions[i][j]) {
                    removeView(item);
                    hodinaViewRecycler.store(item);
                }
                hodinasByCaptions[i][j].clear();
            }
        }

        if (denViews.length == rows && captionViews.length == columns && hodinasByCaptions.length == columns && (hodinasByCaptions.length == 0 || hodinasByCaptions[0].length == rows) && cornerView != null) {
            //debug timing: Log.d(TAG_TIMER, "createViews end " + Utils.getDebugTime());

            return;
        }

        removeAllViews();

        denViews = new DenView[rows];
        captionViews = new CaptionView[columns];

        hodinasByCaptions = new ArrayList[columns][rows];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                hodinasByCaptions[i][j] = new ArrayList<>();
            }
        }


        if (cornerView == null) {
            cornerView = new CornerView(context, null);
        }
        addView(cornerView);

        for (int i = 0; i < columns; i++) {
            CaptionView item = new CaptionView(context, null);
            captionViews[i] = item;
            addView(item);
        }

        for (int i = 0; i < rows; i++) {
            DenView denCell = new DenView(context, null);
            denViews[i] = denCell;
            addView(denCell);
        }

        //debug timing: Log.d(TAG_TIMER, "createViews end " + Utils.getDebugTime());
    }

    public void setRozvrh(Rozvrh rozvrh, boolean centerToCurrentlesson) {
        //debug timing: Log.d(TAG_TIMER, "populate start " + Utils.getDebugTime());
        if (rozvrh != null) {
            Sentry.getContext().addExtra("rozvrh", rozvrh.getStructure());
            Log.d(TAG, "Rozvrh structure:\n" + rozvrh.getStructure());
        } else {
            Sentry.getContext().addExtra("rozvrh", "null");
            Log.d(TAG, "Rozvrh structure:\n" + "null");
        }
        this.rozvrh = rozvrh;
        if (rozvrh == null){
            empty();
            return;
        }

        rows = rozvrh.getDny().size();
        columns = rozvrh.getHodiny().size();
        perm = rozvrh.getTyp().equals("perm");
        columnSizes = new int[columns + 1];
        createViews();

        RozvrhAPI.rememberRows(context, rows);
        RozvrhAPI.rememberColumns(context, columns);

        //populate
        cornerView.setText(rozvrh.getNazevcyklu());
        for (int i = 0; i < columns; i++) {
            CaptionView item = captionViews[i];
            if (item == null) {
                item = new CaptionView(context, null);
                captionViews[i] = item;
                addView(item);
            }
            item.setCaption(rozvrh.getHodiny().get(i));
        }

        for (int i = 0; i < rows; i++) {
            RozvrhDen den = rozvrh.getDny().get(i);
            DenView denCell = denViews[i];
            if (denCell == null) {
                denCell = new DenView(context, null);
                denViews[i] = denCell;
                addView(denCell);
            }
            denViews[i].setRozvrhDen(den);

            String prevCaption = "";
            int captionIndex = 0;
            int j = 0;
            for (; j < den.getHodiny().size(); j++) {
                RozvrhHodina item = den.getHodiny().get(j);

                if (captionIndex >= columns) {
                    Log.w(TAG, "Schedule is having more lessons than there are captions");
                    Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage("Schedule is having more lessons than there are captions").build());
                    captionIndex = columns - 1;
                }

                HodinaView view = hodinaViewRecycler.retrieve();
                view.setHodina(item, perm);
                addView(view);

                //handling more lessons in same time (permanent timetable - different weeks)
                if (item.getCaption() == null || item.getCaption().equals("") || !item.getCaption().equals(prevCaption)) {
                    captionIndex++;
                }

                prevCaption = item.getCaption();
                hodinasByCaptions[captionIndex-1][i].add(view);
            }
        }

        //fill the empty space with empty cells
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (hodinasByCaptions[i][j].size() == 0){
                    HodinaView view = hodinaViewRecycler.retrieve();
                    view.setHodina(null, perm);
                    addView(view);
                    hodinasByCaptions[i][j].add(view);
                }
            }
        }

        highlightCurrentLesson();

        if (centerToCurrentlesson)
            centerToCurrentLesson();

        invalidate();
        requestLayout();

        //debug timing: Log.d(TAG_TIMER, "populate end " + Utils.getDebugTime());
    }

    public void highlightCurrentLesson() {
        if (rozvrh == null){
            nextHodinaView = null;
            nextHodinaViewRight = null;
            nextHodinaViewBottom = null;
            nextHodinaViewCorner = null;
            return;
        }
        Rozvrh.GetNLreturnValues values = rozvrh.getHighlightLesson();


        //unhighlight
        if (nextHodinaView != null) {
            nextHodinaView.hightlightEdges(false, false, false);
            nextHodinaView.highlightEntire(false);
        }
        if (nextHodinaViewRight != null) {
            nextHodinaViewRight.hightlightEdges(false, false, false);
        }
        if (nextHodinaViewBottom != null) {
            nextHodinaViewBottom.hightlightEdges(false, false, false);
        }
        if (nextHodinaViewCorner != null) {
            nextHodinaViewCorner.hightlightEdges(false, false, false);
        }


        nextHodinaView = null;
        nextHodinaViewRight = null;
        nextHodinaViewBottom = null;
        nextHodinaViewCorner = null;
        if (values == null || values.rozvrhHodina == null) {
            return;
        }

        RozvrhHodina hodina = values.rozvrhHodina;
        int denIndex = values.dayIndex;
        int hodinaIndex = values.lessonIndex;

        nextHodinaView = hodinasByCaptions[hodinaIndex][denIndex].get(0);
        //to be extra sure
        if (nextHodinaView.getHodina() != hodina){
            Log.w(TAG, "The empty lessons in the beginning of the day doesn't seem to be there (you expected the opposite)");
            Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage("The empty lessons in the beginning of the day doesn't seem to be there (you expected the opposite)").build());
            for (int i = 0; i < columns; i++) {
                if (hodina == hodinasByCaptions[i][denIndex].get(0).getHodina()){
                    nextHodinaView = hodinasByCaptions[i][denIndex].get(0);
                    hodinaIndex = i;
                }
            }
        }
        nextHodinaView.hightlightEdges(true, true, true);
        nextHodinaView.highlightEntire(true);

        if (denIndex + 1 < rows && hodinaIndex < columns) {
            nextHodinaViewBottom = hodinasByCaptions[hodinaIndex][denIndex + 1].get(0);
            nextHodinaViewBottom.hightlightEdges(true, false, true);
        }
        if (denIndex < rows && hodinaIndex + 1 < columns) {
            nextHodinaViewRight = hodinasByCaptions[hodinaIndex + 1][denIndex].get(0);
            nextHodinaViewRight.hightlightEdges(false, true, true);
        }
        if (denIndex + 1 < rows && hodinaIndex + 1 < columns) {
            nextHodinaViewCorner = hodinasByCaptions[hodinaIndex + 1][denIndex + 1].get(0);
            nextHodinaViewCorner.hightlightEdges(false, false, true);
        }
    }

    // we want to center when: the user opens the app, user taps current week
    // we don't want to center when: a fresh schedule with minor changes loads, user switches to the schedule using arrows.
    public void centerToCurrentLesson(){
        if (!SharedPrefs.getBooleanPreference(context, R.string.PREFS_CENTER_TO_CURRENT_LESSON, true))
            return;
        ViewParent parent = getParent();
        if (parent instanceof HorizontalScrollView){
            HorizontalScrollView hsvParent = (HorizontalScrollView) parent;
            ViewTreeObserver viewTreeObserver = hsvParent.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    RozvrhLayout.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (nextHodinaView != null) {
                        int parentWidth = hsvParent.getWidth();
                        hsvParent.smoothScrollTo(((int) nextHodinaView.getX()) - parentWidth / 2 + nextHodinaView.getWidth() / 2, 0);
                    }
                }
            });
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
        this.rozvrh = null;
        rows = RozvrhAPI.getRememberedRows(getContext());
        columns = RozvrhAPI.getRememberedColumns(getContext());
        perm = false;
        columnSizes = new int[columns + 1];
        createViews();

        //populate
        cornerView.setText("");
        for (int i = 0; i < columns; i++) {
            CaptionView item = captionViews[i];
            if (item == null) {
                item = new CaptionView(context, null);
                captionViews[i] = item;
                addView(item);
            }
            item.setCaption(null);
        }

        for (int i = 0; i < rows; i++) {
            DenView denCell = denViews[i];
            if (denCell == null) {
                denCell = new DenView(context, null);
                denViews[i] = denCell;
                addView(denCell);
            }
            denViews[i].setRozvrhDen(null);
        }

        //fill the empty space with empty cells
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (hodinasByCaptions[i][j].size() == 0){
                    HodinaView view = hodinaViewRecycler.retrieve();
                    view.setHodina(null, perm);
                    addView(view);
                    hodinasByCaptions[i][j].add(view);
                }
            }
        }
        invalidate();
        requestLayout();
        //debug timing: Log.d(TAG_TIMER, "populate end " + Utils.getDebugTime());
    }
}

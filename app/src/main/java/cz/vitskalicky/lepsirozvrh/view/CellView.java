package cz.vitskalicky.lepsirozvrh.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.view.menu.MenuWrapperFactory;
import android.util.AttributeSet;
import android.view.View;

public class CellView extends ConstraintLayout {

    //<editor-fold desc="constructors">
    public CellView(Context context) {
        super(context);
    }

    public CellView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CellView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //</editor-fold>

    private float spread = 1;
    private View top = null;
    private int height;
    private int width;
    private int rows;

    public void init(View top, int rows){
        this.top = top;
        this.rows = rows;
    }

    public void init(View top, int rows, float spread){
        this.init(top, rows);
        setSpread(spread);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int topHeight = top.getMeasuredHeight();
        height = topHeight / rows;
        width = (int) (height * spread);

        int wMSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int hMSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        setMeasuredDimension((int) (height * spread), height);
        super.onMeasure(wMSpec, hMSpec);
    }

    //<editor-fold desc="getters">

    public float getSpread() {
        return spread;
    }

    public View getTopView() {
        return top;
    }

    public int getCalculatedHeight() {
        return height;
    }

    public int getRows() {
        return rows;
    }

    //</editor-fold>


    public void setSpread(float spread) {
        this.spread = spread;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}

package cz.vitskalicky.lepsirozvrh.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.view.menu.MenuWrapperFactory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

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

    public void init(View top, int rows, int width){
        this.top = top;
        this.rows = rows;
        this.width = width;
    }

    public void init(View top, int rows, float spread, int width){
        this.init(top, rows, width);
        setSpread(spread);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int topHeight = top.getMeasuredHeight();
        height = (int) Math.ceil(((double)topHeight / rows)) + 1; //the +1 is to prevent white lines appearing on the bottom
        //width = (int) (height * spread);

        int wMSpec = MeasureSpec.makeMeasureSpec((int) (width * spread), MeasureSpec.EXACTLY);
        int hMSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        setMeasuredDimension((int) ((width * spread)), height);
        super.onMeasure(wMSpec, hMSpec);
    }

    /**
     * Use only on temporal Object - will mess up measurements
     */
    @SuppressLint("WrongCall")
    public int getNaturalWidth(){
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int wMSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int hMSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        super.onMeasure(wMSpec, hMSpec);
        return getMeasuredWidth();
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

    public void setWidth(int width) {
        this.width = width;
    }
}

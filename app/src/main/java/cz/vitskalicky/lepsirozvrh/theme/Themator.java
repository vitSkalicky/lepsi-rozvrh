package cz.vitskalicky.lepsirozvrh.theme;

import android.content.Context;

import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;

import cz.vitskalicky.lepsirozvrh.BuildConfig;
import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.SharedPrefs;

/**
 * takes care of app theme
 * <p>
 * Fallback color is a deep purple, so that it can be noticed by
 */
public class Themator {
    /**
     * Fallback color, deep purple in debug (to be noticeable), grey for release (to be hopefully unnoticed)
     */
    private final static int FALLBACK_COLOR = BuildConfig.DEBUG ? 0xFF00FF : 0x2C2C2C;
    private Context context;

    public Themator(Context context) {
        this.context = context;
    }

    public void applyTheme(Theme theme){
        theme.cyaneaTheme.apply(Cyanea.getInstance());

        setRozvrhBgEmptyColor(theme.cBgEmpty);
        setRozvrhBgAColor(theme.cBgA);
        setRozvrhBgHColor(theme.cBgH);
        setRozvrhBgChngColor(theme.cBgChng);
        setRozvrhBgHeaderColor(theme.cBgHeader);
        setRozvrhDividerColor(theme.cDivider);
        setRozvrhDividerWidth(theme.dpDividerWidth);
        setRozvrhHighlightColor(theme.cHighlight);
        setRozvrhHighlightWidth(theme.dpHighlightWidth);
        setRozvrhPrimaryTextColor(theme.cPrimaryText);
        setRozvrhSecondaryTextColor(theme.cSecondaryText);
        setRozvrhRoomTextColor(theme.cRoomText);
        setRozvrhPrimaryTextSize(theme.spPrimaryText);
        setRozvrhSecondaryTextSize(theme.spSecondaryText);
        setRozvrhPaddingLeft(theme.dpPaddingLeft);
        setRozvrhPaddingTop(theme.dpPaddingTop);
        setRozvrhPaddingRight(theme.dpPaddingRight);
        setRozvrhPaddingBottom(theme.dpPaddingBottom);
        setRozvrhTextPadding(theme.dpTextPadding);
        setInfolineColor(theme.cInfoline);
        setInfolineTextColor(theme.cInfolineText);
        setInfolineTextSize(theme.spInfolineTextSize);
    }

    public Theme constructTheme(){
        Theme theme = new Theme();
        theme.cyaneaTheme = new CyaneaTheme("", Cyanea.getInstance());
        theme.cBgEmpty = getRozvrhBgEmptyColor();
        theme.cBgA = getRozvrhBgAColor();
        theme.cBgH = getRozvrhBgHColor();
        theme.cBgChng = getRozvrhBgChngColor();
        theme.cBgHeader = getRozvrhBgHeaderColor();
        theme.cDivider = getRozvrhDividerColor();
        theme.dpDividerWidth = getRozvrhDividerWidth();
        theme.cHighlight = getRozvrhHighlightColor();
        theme.dpHighlightWidth = getRozvrhHighlightWidth();
        theme.cPrimaryText = getRozvrhPrimaryTextColor();
        theme.cSecondaryText = getRozvrhSecondaryTextColor();
        theme.cRoomText = getRozvrhRoomTextColor();
        theme.spPrimaryText = getRozvrhPrimaryTextSize();
        theme.spSecondaryText = getRozvrhSecondaryTextSize();
        theme.dpPaddingLeft = getRozvrhPaddingLeft();
        theme.dpPaddingTop = getRozvrhPaddingTop();
        theme.dpPaddingRight = getRozvrhPaddingRight();
        theme.dpPaddingBottom = getRozvrhPaddingBottom();
        theme.dpTextPadding = getRozvrhTextPadding();
        theme.cInfoline = getInfolineColor();
        theme.cInfolineText = getInfolineTextColor();
        theme.spInfolineTextSize = getInfolineTextSize();

        return theme;
    }

    /*public static Theme getDefaultTheme(){
        CyaneaTheme cyaneaTheme = new CyaneaTheme("", new Cyanea)
    }*/

    public void applyDefaultTheme(){
        Cyanea.getInstance().edit()
                .primary(0xf9a825)
                .accent(0x455a64)
                .baseTheme(Cyanea.BaseTheme.LIGHT);

        Theme theme = new Theme();
        theme.cyaneaTheme = new CyaneaTheme("", Cyanea.getInstance());

        theme.cBgChng = 0xffd95a;
        theme.cBgA = 0xffd95a;
        theme.cBgH = 0xeceff1;
        theme.cBgEmpty = 0x00FFFFFF;
        theme.cBgHeader = 0xCFD8DC;
        theme.cDivider = 0x607D8B;
        theme.dpDividerWidth = 1;
        theme.cHighlight = 0xf9a825;
        theme.dpHighlightWidth = 2;
        theme.cPrimaryText = 0x000000;
        theme.cRoomText = 0x000000;
        theme.cSecondaryText = 0x607D8B;
        theme.spPrimaryText = 18;
        theme.spSecondaryText = 12;
        theme.dpPaddingLeft = 3;
        theme.dpPaddingTop = 3;
        theme.dpPaddingRight = 3;
        theme.dpPaddingBottom = 3;
        theme.dpTextPadding = 2;

        applyTheme(theme);
    }



    public int getRozvrhBgEmptyColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_EMPTY, FALLBACK_COLOR);
    }
    public int getRozvrhBgAColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_A, FALLBACK_COLOR);
    }
    public int getRozvrhBgHColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_H, FALLBACK_COLOR);
    }
    public int getRozvrhBgChngColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_CHNG, FALLBACK_COLOR);
    }
    public int getRozvrhBgHeaderColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_HEADER, FALLBACK_COLOR);
    }
    public int getRozvrhDividerColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_DIVIDER, FALLBACK_COLOR);
    }
    /**
     * In px
     */
    public int getRozvrhDividerWidth() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_DIVIDER, 10) * context.getResources().getDisplayMetrics().density);
    }
    public int getRozvrhHighlightColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HIGHLIGHT, FALLBACK_COLOR);
    }
    /**
     * In px
     */
    public int getRozvrhHighlightWidth() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_HIGHLIGHT, 10) * context.getResources().getDisplayMetrics().density);
    }
    public int getRozvrhPrimaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_PRIMARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhSecondaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_SECONDARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhRoomTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_ROOM_TEXT, FALLBACK_COLOR);
    }
    /**
     * In px
     */
    public int getRozvrhPrimaryTextSize() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_SP_PRIMARY_TEXT, 10) * context.getResources().getDisplayMetrics().scaledDensity);
    }
    /**
     * In px
     */
    public int getRozvrhSecondaryTextSize() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_SP_SECONDARY_TEXT, 10) * context.getResources().getDisplayMetrics().scaledDensity);
    }
    /**
     * In px
     */
    public int getRozvrhPaddingLeft() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_LEFT, 10) * context.getResources().getDisplayMetrics().density);
    }
    /**
     * In px
     */
    public int getRozvrhPaddingTop() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_TOP, 10) * context.getResources().getDisplayMetrics().density);
    }
    /**
     * In px
     */
    public int getRozvrhPaddingRight() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_RIGHT, 10) * context.getResources().getDisplayMetrics().density);
    }
    /**
     * In px
     */
    public int getRozvrhPaddingBottom() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_BOTTOM, 10) * context.getResources().getDisplayMetrics().density);
    }
    /**
     * In px
     */
    public int getRozvrhTextPadding() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_TEXT_PADDING, 10) * context.getResources().getDisplayMetrics().density);
    }
    public int getInfolineColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_INFOLINE_COLOR, FALLBACK_COLOR);
    }
    public int getInfolineTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_INFOLINE_COLOR_TEXT, FALLBACK_COLOR);
    }
    /**
     * In px
     */
    public int getInfolineTextSize() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_INFOLINE_SP_TEXT, 10) * context.getResources().getDisplayMetrics().scaledDensity);
    }





    public void setRozvrhBgEmptyColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_EMPTY, color);
    }

    public void setRozvrhBgAColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_A, color);
    }

    public void setRozvrhBgHColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_H, color);
    }

    public void setRozvrhBgChngColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_CHNG, color);
    }

    public void setRozvrhBgHeaderColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_HEADER, color);
    }

    public void setRozvrhDividerColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_DIVIDER, color);
    }

    public void setRozvrhDividerWidth(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_DP_DIVIDER, value);
    }

    public void setRozvrhHighlightColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HIGHLIGHT, color);
    }

    public void setRozvrhHighlightWidth(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_DP_HIGHLIGHT, value);
    }

    public void setRozvrhPrimaryTextColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_PRIMARY_TEXT, color);
    }

    public void setRozvrhSecondaryTextColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_SECONDARY_TEXT, color);
    }

    public void setRozvrhRoomTextColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_ROOM_TEXT, color);
    }

    public void setRozvrhPrimaryTextSize(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_SP_PRIMARY_TEXT, value);
    }

    public void setRozvrhSecondaryTextSize(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_SP_SECONDARY_TEXT, value);
    }

    public void setRozvrhPaddingLeft(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_LEFT, value);
    }

    public void setRozvrhPaddingTop(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_TOP, value);
    }

    public void setRozvrhPaddingRight(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_RIGHT, value);
    }

    public void setRozvrhPaddingBottom(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_BOTTOM, value);
    }

    public void setRozvrhTextPadding(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_ROZVRH_DP_TEXT_PADDING, value);
    }

    public void setInfolineColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_INFOLINE_COLOR, color);
    }

    public void setInfolineTextColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_INFOLINE_COLOR_TEXT, color);
    }

    public void setInfolineTextSize(float value) {
        SharedPrefs.setFloatPreference(context, R.string.THEME_INFOLINE_SP_TEXT, value);
    }

    
}

package cz.vitskalicky.lepsirozvrh.theme;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        setRozvrhHodinaPrimaryTextColor(theme.cHodinaPrimaryText);
        setRozvrhHodinaSecondaryTextColor(theme.cHodinaSecondaryText);
        setRozvrhHodinaRoomTextColor(theme.cHodinaRoomText);
        setRozvrhHodinaChngPrimaryTextColor(theme.cHodinaChngPrimaryText);
        setRozvrhHodinaChngSecondaryTextColor(theme.cHodinaChngSecondaryText);
        setRozvrhHodinaChngRoomTextColor(theme.cHodinaChngRoomText);
        setRozvrhHodinaAPrimaryTextColor(theme.cHodinaAPrimaryText);
        setRozvrhHodinaASecondaryTextColor(theme.cHodinaASecondaryText);
        setRozvrhHodinaARoomTextColor(theme.cHodinaARoomText);
        setRozvrhHeaderPrimaryTextColor(theme.cCaptionPrimaryText);
        setRozvrhHeaderSecondaryTextColor(theme.cCaptionSecondaryText);
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
        theme.dpDividerWidth = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_DIVIDER, 1);
        theme.cHighlight = getRozvrhHighlightColor();
        theme.dpHighlightWidth = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_HIGHLIGHT, 1);
        theme.cHodinaPrimaryText = getRozvrhHodinaPrimaryTextColor();
        theme.cHodinaSecondaryText = getRozvrhHodinaSecondaryTextColor();
        theme.cHodinaRoomText = getRozvrhHodinaRoomTextColor();
        theme.cHodinaChngPrimaryText = getRozvrhHodinaChngPrimaryTextColor();
        theme.cHodinaChngSecondaryText = getRozvrhHodinaChngSecondaryTextColor();
        theme.cHodinaChngRoomText = getRozvrhHodinaChngRoomTextColor();
        theme.cHodinaAPrimaryText = getRozvrhHodinaAPrimaryTextColor();
        theme.cHodinaASecondaryText = getRozvrhHodinaASecondaryTextColor();
        theme.cHodinaARoomText = getRozvrhHodinaARoomTextColor();
        theme.cCaptionPrimaryText = getRozvrhHeaderPrimaryTextColor();
        theme.cCaptionSecondaryText = getRozvrhHeaderSecondaryTextColor();
        theme.spPrimaryText = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_SP_PRIMARY_TEXT, 10);
        theme.spSecondaryText = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_SP_SECONDARY_TEXT, 10);
        theme.dpPaddingLeft = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_LEFT, 2);
        theme.dpPaddingTop = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_TOP, 1);
        theme.dpPaddingRight = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_RIGHT, 2);
        theme.dpPaddingBottom = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_BOTTOM, 1);
        theme.dpTextPadding = SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_TEXT_PADDING, 1);
        theme.cInfoline = getInfolineColor();
        theme.cInfolineText = getInfolineTextColor();
        theme.spInfolineTextSize = SharedPrefs.getFloatPreference(context, R.string.THEME_INFOLINE_SP_TEXT, 10);

        return theme;
    }

    public boolean writeCurrentTheme(OutputStream os){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(os, constructTheme());
            String json = mapper.writeValueAsString(constructTheme());
            System.out.println(json);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadTheme(InputStream is){
        ObjectMapper mapper = new ObjectMapper();
        try{
            Theme theme = mapper.readValue(is, Theme.class);
            applyTheme(theme);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*public static Theme getDefaultTheme(){
        CyaneaTheme cyaneaTheme = new CyaneaTheme("", new Cyanea)
    }*/

    public void applyDefaultTheme(){


        Theme theme = new Theme();
        theme.cyaneaTheme = new CyaneaTheme("", Cyanea.getInstance());

        theme.cBgChng = 0xffffd95a;
        theme.cBgA = 0xffffd95a;
        theme.cBgH = 0xffeceff1;
        theme.cBgEmpty = 0xFFFFFFFF;
        theme.cBgHeader = 0xffCFD8DC;
        theme.cDivider = 0xff607D8B;
        theme.dpDividerWidth = 1;
        theme.cHighlight = 0xfff9a825;
        theme.dpHighlightWidth = 2;
        theme.cHodinaPrimaryText = 0xff000000;
        theme.cHodinaRoomText = 0xff000000;
        theme.cHodinaSecondaryText = 0xff607D8B;
        theme.cHodinaChngPrimaryText = 0xff000000;
        theme.cHodinaChngRoomText = 0xff000000;
        theme.cHodinaChngSecondaryText = 0xff607D8B;
        theme.cHodinaAPrimaryText = 0xff000000;
        theme.cHodinaARoomText = 0xff000000;
        theme.cHodinaASecondaryText = 0xff607D8B;
        theme.cCaptionPrimaryText = 0xff000000;
        theme.cCaptionSecondaryText = 0xff607D8B;
        theme.spPrimaryText = 18;
        theme.spSecondaryText = 12;
        theme.dpPaddingLeft = 3;
        theme.dpPaddingTop = 3;
        theme.dpPaddingRight = 3;
        theme.dpPaddingBottom = 3;
        theme.dpTextPadding = 2;

        applyTheme(theme);
        Cyanea.getInstance().edit()
                .primary(0xfff9a825)
                .accent(0xff455a64)
                .baseTheme(Cyanea.BaseTheme.LIGHT);
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
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_DIVIDER, 1) * context.getResources().getDisplayMetrics().density);
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
    public int getRozvrhHodinaPrimaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_PRIMARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHodinaSecondaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_SECONDARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHodinaRoomTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_ROOM_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHodinaChngPrimaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_PRIMARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHodinaChngSecondaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_SECONDARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHodinaChngRoomTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_ROOM_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHodinaAPrimaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_A_PRIMARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHodinaASecondaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_A_SECONDARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHodinaARoomTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_A_ROOM_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHeaderPrimaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HEADER_PRIMARY_TEXT, FALLBACK_COLOR);
    }
    public int getRozvrhHeaderSecondaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HEADER_SECONDARY_TEXT, FALLBACK_COLOR);
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
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_LEFT, 2) * context.getResources().getDisplayMetrics().density);
    }
    /**
     * In px
     */
    public int getRozvrhPaddingTop() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_TOP, 1) * context.getResources().getDisplayMetrics().density);
    }
    /**
     * In px
     */
    public int getRozvrhPaddingRight() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_RIGHT, 2) * context.getResources().getDisplayMetrics().density);
    }
    /**
     * In px
     */
    public int getRozvrhPaddingBottom() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_BOTTOM, 1) * context.getResources().getDisplayMetrics().density);
    }
    /**
     * In px
     */
    public int getRozvrhTextPadding() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_TEXT_PADDING, 1) * context.getResources().getDisplayMetrics().density);
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

    public void setRozvrhHodinaPrimaryTextColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_PRIMARY_TEXT, color);
    }

    public void setRozvrhHodinaSecondaryTextColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_SECONDARY_TEXT, color);
    }

    public void setRozvrhHodinaRoomTextColor(int color) {
        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_ROOM_TEXT, color);
    }

    public void setRozvrhHodinaChngPrimaryTextColor(int color) {        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_PRIMARY_TEXT, color);    }
    public void setRozvrhHodinaChngSecondaryTextColor(int color) {        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_SECONDARY_TEXT, color);    }
    public void setRozvrhHodinaChngRoomTextColor(int color) {        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_CHNG_ROOM_TEXT, color);    }
    public void setRozvrhHodinaAPrimaryTextColor(int color) {        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_A_PRIMARY_TEXT, color);    }
    public void setRozvrhHodinaASecondaryTextColor(int color) {        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_A_SECONDARY_TEXT, color);    }
    public void setRozvrhHodinaARoomTextColor(int color) {        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HODINA_A_ROOM_TEXT, color);    }
    public void setRozvrhHeaderPrimaryTextColor(int color) {        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HEADER_PRIMARY_TEXT, color);    }
    public void setRozvrhHeaderSecondaryTextColor(int color) {        SharedPrefs.setIntPreference(context, R.string.THEME_ROZVRH_COLOR_HEADER_SECONDARY_TEXT, color);    }

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

package cz.vitskalicky.lepsirozvrh.theme;

import android.content.Context;

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
 * Class for managing theme, setting values and getting values.
 *
 * 'c' for color, 'dp' for dimension in dp, 'sp' for text size in sp, etc.
 * 'H' for normal lesson (stands for 'Hodina'), 'Chng' for changed, 'A' for no school (stands for 'A0bsence', probably. The Bakláři API just calls it so).
 * Example: cHBg = color of normal lesson background
 */
public class Theme {
    /**
     * Fallback color, deep purple in debug (to be noticeable), grey for release (to be hopefully unnoticed)
     */
    private final static int FALLBACK_COLOR = BuildConfig.DEBUG ? 0xFFFF00FF : 0xFF2C2C2C;
    private Context context;

    public void setThemeData(ThemeData td){

        td.cyaneaTheme.apply(getCyanea());

        setCEmptyBg(td.cEmptyBg);
        setCABg(td.cABg);
        setCHBg(td.cHBg);
        setCChngBg(td.cChngBg);
        setCHeaderBg(td.cHeaderBg);
        setCDivider(td.cDivider);
        setDpDividerWidth(td.dpDividerWidth);
        setCHighlight(td.cHighlight);
        setDpHighlightWidth(td.dpHighlightWidth);
        setCHPrimaryText(td.cHPrimaryText);
        setCHRoomText(td.cHRoomText);
        setCHSecondaryText(td.cHSecondaryText);
        setCChngPrimaryText(td.cChngPrimaryText);
        setCChngRoomText(td.cChngRoomText);
        setCChngSecondaryText(td.cChngSecondaryText);
        setCAPrimaryText(td.cAPrimaryText);
        setCARoomText(td.cARoomText);
        setCASecondaryText(td.cASecondaryText);
        setCHeaderPrimaryText(td.cHeaderPrimaryText);
        setCHeaderSecondaryText(td.cHeaderSecondaryText);
        setSpPrimaryText(td.spPrimaryText);
        setSpSecondaryText(td.spSecondaryText);
        setDpPaddingLeft(td.dpPaddingLeft);
        setDpPaddingTop(td.dpPaddingTop);
        setDpPaddingRight(td.dpPaddingRight);
        setDpPaddingBottom(td.dpPaddingBottom);
        setDpTextPadding(td.dpTextPadding);
        setCInfolineBg(td.cInfolineBg);
        setCInfolineText(td.cInfolineText);
        setSpInfolineTextSize(td.spInfolineTextSize);
    }

    public ThemeData getThemeData(){
        ThemeData td = new ThemeData();

        td.cyaneaTheme = new CyaneaTheme("",getCyanea());

        td.cEmptyBg = getCEmptyBg();
        td.cABg = getCABg();
        td.cHBg = getCHBg();
        td.cChngBg = getCChngBg();
        td.cHeaderBg = getCHeaderBg();
        td.cDivider = getCDivider();
        td.dpDividerWidth = getDpDividerWidth();
        td.cHighlight = getCHighlight();
        td.dpHighlightWidth = getDpHighlightWidth();
        td.cHPrimaryText = getCHPrimaryText();
        td.cHRoomText = getCHRoomText();
        td.cHSecondaryText = getCHSecondaryText();
        td.cChngPrimaryText = getCChngPrimaryText();
        td.cChngRoomText = getCChngRoomText();
        td.cChngSecondaryText = getCChngSecondaryText();
        td.cAPrimaryText = getCAPrimaryText();
        td.cARoomText = getCARoomText();
        td.cASecondaryText = getCASecondaryText();
        td.cHeaderPrimaryText = getCHeaderPrimaryText();
        td.cHeaderSecondaryText = getCHeaderSecondaryText();
        td.spPrimaryText = getSpPrimaryText();
        td.spSecondaryText = getSpSecondaryText();
        td.dpPaddingLeft = getDpPaddingLeft();
        td.dpPaddingTop = getDpPaddingTop();
        td.dpPaddingRight = getDpPaddingRight();
        td.dpPaddingBottom = getDpPaddingBottom();
        td.dpTextPadding = getDpTextPadding();
        td.cInfolineBg = getCInfolineBg();
        td.cInfolineText = getCInfolineText();
        td.spInfolineTextSize = getSpInfolineTextSize();

        return td;
    }

    public boolean writeCurrentThemeData(OutputStream os){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(os, getThemeData());
            //todo:this is debug
            String json = mapper.writeValueAsString(getThemeData());
            System.out.println(json);
            return true;
        } catch (IOException e) {
            new IOException("Failed to write current theme",e).printStackTrace();
            return false;
        }
    }

    public boolean loadThemeData(InputStream is){
        ObjectMapper mapper = new ObjectMapper();
        try{
            ThemeData theme = mapper.readValue(is, ThemeData.class);
            setThemeData(theme);
            return true;
        } catch (IOException e) {
            new IOException("Failed to load theme",e).printStackTrace();
            return false;
        }
    }

    public void useDefaultTheme(){
        setCABg(0xffffd95a);
        setCEmptyBg(0x00FFFFFF);
        setCHBg(0xffeceff1);
        setCChngBg(0xffffd95a);
        setCHeaderBg(0xffCFD8DC);
        setCDivider(0xff607D8B);
        setDpDividerWidth(1);
        setCHighlight(0xfff9a825);
        setDpHighlightWidth(2);
        setCHPrimaryText(0xff000000);
        setCHRoomText(0xff000000);
        setCHSecondaryText(0xff607D8B);
        setCChngPrimaryText(0xff000000);
        setCChngRoomText(0xff000000);
        setCChngSecondaryText(0xff607D8B);
        setCAPrimaryText(0xff000000);
        setCARoomText(0xff000000);
        setCASecondaryText(0xff607D8B);
        setCHeaderPrimaryText(0xff000000);
        setCHeaderSecondaryText(0xff607D8B);
        setSpPrimaryText(18);
        setSpSecondaryText(12);
        setDpPaddingLeft(3);
        setDpPaddingTop(3);
        setDpPaddingRight(3);
        setDpPaddingBottom(3);
        setDpTextPadding(2);
        setCInfolineBg(0xff424242);
        setCInfolineText(0xffFFFFFF);
        setSpInfolineTextSize(12);

        getCyanea().edit()
                .primary(0xfff9a825)
                .accent(0xff455a64)
                .baseTheme(Cyanea.BaseTheme.LIGHT)
                .apply();
    }

    /**
     * Primary, accent, background and other standard colors are here
     */
    public Cyanea getCyanea(){
        return Cyanea.getInstance();
    }

    public int getCPrimary(){
        return getCyanea().getPrimary();
    }
    public int getCAccent(){
        return getCyanea().getAccent();
    }
    //Edit primary and accent using getCyanea().edit().primary(color).apply();


    //@formatter:off
    public void setCEmptyBg(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cEmptyBg, color); }
    public void setCABg(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cABg, color); }
    public void setCHBg(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cHBg, color); }
    public void setCChngBg(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cChngBg, color); }
    public void setCHeaderBg(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cHeaderBg, color); }
    public void setCDivider(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cDivider, color); }
    public void setDpDividerWidth(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_dpDividerWidth, size); }
    public void setCHighlight(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cHighlight, color); }
    public void setDpHighlightWidth(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_dpHighlightWidth, size); }
    public void setCHPrimaryText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cHPrimaryText, color); }
    public void setCHRoomText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cHRoomText, color); }
    public void setCHSecondaryText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cHSecondaryText, color); }
    public void setCChngPrimaryText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cChngPrimaryText, color); }
    public void setCChngRoomText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cChngRoomText, color); }
    public void setCChngSecondaryText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cChngSecondaryText, color); }
    public void setCAPrimaryText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cAPrimaryText, color); }
    public void setCARoomText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cARoomText, color); }
    public void setCASecondaryText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cASecondaryText, color); }
    public void setCHeaderPrimaryText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cHeaderPrimaryText, color); }
    public void setCHeaderSecondaryText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cHeaderSecondaryText, color); }
    public void setSpPrimaryText(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_spPrimaryText, size); }
    public void setSpSecondaryText(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_spSecondaryText, size); }
    public void setDpPaddingLeft(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_dpPaddingLeft, size); }
    public void setDpPaddingTop(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_dpPaddingTop, size); }
    public void setDpPaddingRight(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_dpPaddingRight, size); }
    public void setDpPaddingBottom(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_dpPaddingBottom, size); }
    public void setDpTextPadding(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_dpTextPadding, size); }
    public void setCInfolineBg(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cInfolineBg, color); }
    public void setCInfolineText(int color){ SharedPrefs.setIntPreference(context, R.string.PREFS_THEME_cInfolineText, color); }
    public void setSpInfolineTextSize(float size){ SharedPrefs.setFloatPreference(context, R.string.PREFS_THEME_spInfolineTextSize, size); }

    public int getCEmptyBg(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cEmptyBg, FALLBACK_COLOR);}
    public int getCABg(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cABg, FALLBACK_COLOR);}
    public int getCHBg(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cHBg, FALLBACK_COLOR);}
    public int getCChngBg(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cChngBg, FALLBACK_COLOR);}
    public int getCHeaderBg(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cHeaderBg, FALLBACK_COLOR);}
    public int getCDivider(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cDivider, FALLBACK_COLOR);}
    public float getDpDividerWidth(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_dpDividerWidth, 1.0f);}
    public int getCHighlight(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cHighlight, FALLBACK_COLOR);}
    public float getDpHighlightWidth(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_dpHighlightWidth, 1.0f);}
    public int getCHPrimaryText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cHPrimaryText, FALLBACK_COLOR);}
    public int getCHRoomText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cHRoomText, FALLBACK_COLOR);}
    public int getCHSecondaryText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cHSecondaryText, FALLBACK_COLOR);}
    public int getCChngPrimaryText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cChngPrimaryText, FALLBACK_COLOR);}
    public int getCChngRoomText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cChngRoomText, FALLBACK_COLOR);}
    public int getCChngSecondaryText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cChngSecondaryText, FALLBACK_COLOR);}
    public int getCAPrimaryText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cAPrimaryText, FALLBACK_COLOR);}
    public int getCARoomText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cARoomText, FALLBACK_COLOR);}
    public int getCASecondaryText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cASecondaryText, FALLBACK_COLOR);}
    public int getCHeaderPrimaryText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cHeaderPrimaryText, FALLBACK_COLOR);}
    public int getCHeaderSecondaryText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cHeaderSecondaryText, FALLBACK_COLOR);}
    public float getSpPrimaryText(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_spPrimaryText, 12.0f);}
    public float getSpSecondaryText(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_spSecondaryText, 10.0f);}
    public float getDpPaddingLeft(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_dpPaddingLeft, 2.0f);}
    public float getDpPaddingTop(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_dpPaddingTop, 2.0f);}
    public float getDpPaddingRight(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_dpPaddingRight, 2.0f);}
    public float getDpPaddingBottom(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_dpPaddingBottom, 2.0f);}
    public float getDpTextPadding(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_dpTextPadding, 2.0f);}
    public int getCInfolineBg(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cInfolineBg, FALLBACK_COLOR);}
    public int getCInfolineText(){ return SharedPrefs.getIntPreference(context, R.string.PREFS_THEME_cInfolineText, FALLBACK_COLOR);}
    public float getSpInfolineTextSize(){ return SharedPrefs.getFloatPreference(context, R.string.PREFS_THEME_spInfolineTextSize, 10.0f);}
    //@formatter:on

    /*
cEmptyBg                int color
cABg                    int color
cHBg                    int color
cChngBg                 int color
cHeaderBg               int color
cDivider                int color
dpDividerWidth          float size
cHighlight              int color
dpHighlightWidth        float size
cHPrimaryText           int color
cHRoomText              int color
cHSecondaryText         int color
cChngPrimaryText        int color
cChngRoomText           int color
cChngSecondaryText      int color
cAPrimaryText           int color
cARoomText              int color
cASecondaryText         int color
cHeaderPrimaryText      int color
cHeaderSecondaryText    int color
spPrimaryText           float size
spSecondaryText         float size
dpPaddingLeft           float size
dpPaddingTop            float size
dpPaddingRight          float size
dpPaddingBottom         float size
dpTextPadding           float size
cInfolineBg             int color
cInfolineText           int color
spInfolineTextSize      float size
     */
}

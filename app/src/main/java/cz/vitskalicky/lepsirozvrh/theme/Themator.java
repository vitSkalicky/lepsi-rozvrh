package cz.vitskalicky.lepsirozvrh.theme;

import android.content.Context;

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

    public int getRozvrhBgEmptyColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_EMPTY);
    }

    public int getRozvrhBgAColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_A);
    }

    public int getRozvrhBgHColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_H);
    }

    public int getRozvrhBgChngColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_CHNG);
    }

    public int getRozvrhBgHeaderColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_BG_HEADER);
    }

    public int getRozvrhDividerColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_DIVIDER);
    }

    /**
     * In px
     */
    public int getRozvrhDividerWidth() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_DIVIDER) * context.getResources().getDisplayMetrics().density);
    }

    public int getRozvrhHighlightColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_HIGHLIGHT);
    }

    /**
     * In px
     */
    public int getRozvrhHighlightWidth() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_HIGHLIGHT) * context.getResources().getDisplayMetrics().density);
    }

    public int getRozvrhPrimaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_PRIMARY_TEXT);
    }

    public int getRozvrhSecondaryTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_SECONDARY_TEXT);
    }

    public int getRozvrhRoomTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_ROZVRH_COLOR_ROOM_TEXT);
    }

    /**
     * In px
     */
    public int getRozvrhPrimaryTextSize() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_SP_PRIMARY_TEXT) * context.getResources().getDisplayMetrics().scaledDensity);
    }

    /**
     * In px
     */
    public int getRozvrhSecondaryTextSize() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_SP_SECONDARY_TEXT) * context.getResources().getDisplayMetrics().scaledDensity);
    }

    /**
     * In px
     */
    public int getRozvrhPaddingLeft() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_LEFT) * context.getResources().getDisplayMetrics().density);
    }

    /**
     * In px
     */
    public int getRozvrhPaddingTop() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_TOP) * context.getResources().getDisplayMetrics().density);
    }

    /**
     * In px
     */
    public int getRozvrhPaddingRight() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_RIGHT) * context.getResources().getDisplayMetrics().density);
    }

    /**
     * In px
     */
    public int getRozvrhPaddingBottom() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_PADDING_BOTTOM) * context.getResources().getDisplayMetrics().density);
    }

    /**
     * In px
     */
    public int getRozvrhTextPadding() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_ROZVRH_DP_TEXT_PADDING) * context.getResources().getDisplayMetrics().density);
    }

    public int getInfolineColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_INFOLINE_COLOR);
    }

    public int getInfolineTextColor() {
        return SharedPrefs.getIntPreference(context, R.string.THEME_INFOLINE_COLOR_TEXT);
    }

    /**
     * In px
     */
    public int getInfolineTextSize() {
        return (int) (SharedPrefs.getFloatPreference(context, R.string.THEME_INFOLINE_SP_TEXT) * context.getResources().getDisplayMetrics().scaledDensity);
    }
}

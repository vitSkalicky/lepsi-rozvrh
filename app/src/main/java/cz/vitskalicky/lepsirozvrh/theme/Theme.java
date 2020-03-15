package cz.vitskalicky.lepsirozvrh.theme;

import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;

/**
 * Class containing theme values. JSONable.
 *
 * 'c' for color, 'dp' for dimension in dp, 'sp' for text size in sp, etc.
 */
public class Theme {
    /**
     * the basic values such as primary, accent acolors, etc. are stored here.
     */
    public CyaneaTheme cyaneaTheme;

    // my addition values
    // most of them, are for cell views

    public int cBgEmpty;
    public int cBgA;
    public int cBgH;
    public int cBgChng;
    public int cBgHeader;

    public int cDivider;
    public int dpDividerWidth;
    public int cHighlight; //for current lesson
    public int dpHighlightWidth;

    public int cHodinaPrimaryText;
    public int cHodinaRoomText;
    public int cHodinaSecondaryText;
    public int cHodinaChngPrimaryText;
    public int cHodinaChngRoomText;
    public int cHodinaChngSecondaryText;
    public int cHodinaAPrimaryText;
    public int cHodinaARoomText;
    public int cHodinaASecondaryText;
    public int cCaptionPrimaryText;
    public int cCaptionSecondaryText;
    public int spPrimaryText;
    public int spSecondaryText;

    public int dpPaddingLeft;
    public int dpPaddingTop;
    public int dpPaddingRight;
    public int dpPaddingBottom;
    public int dpTextPadding;

    // info line
    public int cInfoline;
    public int cInfolineText;
    public int spInfolineTextSize;
}

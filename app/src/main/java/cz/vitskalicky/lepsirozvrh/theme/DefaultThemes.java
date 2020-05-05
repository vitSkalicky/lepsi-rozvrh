package cz.vitskalicky.lepsirozvrh.theme;

import java.io.IOException;

public class DefaultThemes {
    public static final String LIGHT = "{\"cABg\":\"FFD95A\",\"cAPrimaryText\":\"000000\",\"cARoomText\":\"000000\",\"cASecondaryText\":\"607D8B\",\"cChngBg\":\"FFD95A\",\"cChngPrimaryText\":\"000000\",\"cChngRoomText\":\"000000\",\"cChngSecondaryText\":\"607D8B\",\"cDivider\":\"607D8B\",\"cEmptyBg\":\"FFFFFF\",\"cError\":\"B00020\",\"cHBg\":\"ECEFF1\",\"cHPrimaryText\":\"000000\",\"cHRoomText\":\"000000\",\"cHSecondaryText\":\"607D8B\",\"cHeaderBg\":\"CFD8DC\",\"cHeaderPrimaryText\":\"000000\",\"cHeaderSecondaryText\":\"607D8B\",\"cHighlight\":\"F9A825\",\"cHomework\":\"ef5350\",\"cInfolineBg\":\"424242\",\"cInfolineText\":\"FFFFFF\",\"cyaneaTheme\":{\"primary\":\"F9A825\",\"accent\":\"455A64\",\"background\":\"FAFAFA\"},\"dpDividerWidth\":1.0,\"dpHighlightWidth\":2.0,\"dpHomework\":5.0,\"dpPaddingBottom\":3.0,\"dpPaddingLeft\":3.0,\"dpPaddingRight\":3.0,\"dpPaddingTop\":3.0,\"dpTextPadding\":2.0,\"spInfolineTextSize\":12.0,\"spPrimaryText\":18.0,\"spSecondaryText\":12.0}";
    public static final String DARK = "{\"cABg\":\"FF9800\",\"cAPrimaryText\":\"FFFFFF\",\"cARoomText\":\"D2D7D9\",\"cASecondaryText\":\"FFFFFF\",\"cChngBg\":\"FF9800\",\"cChngPrimaryText\":\"FFFFFF\",\"cChngRoomText\":\"D2D7D9\",\"cChngSecondaryText\":\"FFFFFF\",\"cDivider\":\"121212\",\"cEmptyBg\":\"121212\",\"cError\":\"B00020\",\"cHBg\":\"1F1F1F\",\"cHPrimaryText\":\"FFFFFF\",\"cHRoomText\":\"D2D7D9\",\"cHSecondaryText\":\"FFFFFF\",\"cHeaderBg\":\"393939\",\"cHeaderPrimaryText\":\"FFFFFF\",\"cHeaderSecondaryText\":\"FFFFFF\",\"cHighlight\":\"F9A825\",\"cHomework\":\"FF9800\",\"cInfolineBg\":\"424242\",\"cInfolineText\":\"FFFFFF\",\"cyaneaTheme\":{\"primary\":\"F9A825\",\"accent\":\"CFD8DC\",\"background\":\"121212\"},\"dpDividerWidth\":1.0,\"dpHighlightWidth\":1.0,\"dpHomework\":5.0,\"dpPaddingBottom\":3.0,\"dpPaddingLeft\":3.0,\"dpPaddingRight\":3.0,\"dpPaddingTop\":3.0,\"dpTextPadding\":2.0,\"spInfolineTextSize\":12.0,\"spPrimaryText\":18.0,\"spSecondaryText\":12.0}";
    public static final String BLACK = "{\"cABg\":\"464C4F\",\"cAPrimaryText\":\"FFFFFF\",\"cARoomText\":\"E3E8EB\",\"cASecondaryText\":\"FFFFFF\",\"cChngBg\":\"464C4F\",\"cChngPrimaryText\":\"FFFFFF\",\"cChngRoomText\":\"E3E8EB\",\"cChngSecondaryText\":\"FFFFFF\",\"cDivider\":\"121212\",\"cEmptyBg\":\"121212\",\"cError\":\"B00020\",\"cHBg\":\"000000\",\"cHPrimaryText\":\"FFFFFF\",\"cHRoomText\":\"E3E8EB\",\"cHSecondaryText\":\"FFFFFF\",\"cHeaderBg\":\"242424\",\"cHeaderPrimaryText\":\"FFFFFF\",\"cHeaderSecondaryText\":\"FFFFFF\",\"cHighlight\":\"F9A825\",\"cHomework\":\"F9A825\",\"cInfolineBg\":\"424242\",\"cInfolineText\":\"FFFFFF\",\"cyaneaTheme\":{\"primary\":\"F9A825\",\"accent\":\"CFD8DC\",\"background\":\"000000\"},\"dpDividerWidth\":1.5,\"dpHighlightWidth\":1.0,\"dpHomework\":5.0,\"dpPaddingBottom\":3.0,\"dpPaddingLeft\":3.0,\"dpPaddingRight\":3.0,\"dpPaddingTop\":3.0,\"dpTextPadding\":2.0,\"spInfolineTextSize\":12.0,\"spPrimaryText\":18.0,\"spSecondaryText\":12.0}";

    public static ThemeData getLightTheme(){
        try {
            return ThemeData.parseJson(LIGHT);
        } catch (IOException e) {
            //this should never happen
            return null;
        }
    }
    public static ThemeData getDarkTheme(){
        try {
            return ThemeData.parseJson(DARK);
        } catch (IOException e) {
            //this should never happen
            return null;
        }
    }
    public static ThemeData getBlackTheme(){
        try {
            return ThemeData.parseJson(BLACK);
        } catch (IOException e) {
            //this should never happen
            return null;
        }
    }
}

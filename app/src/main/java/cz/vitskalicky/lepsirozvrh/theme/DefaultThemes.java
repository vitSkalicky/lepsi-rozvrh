package cz.vitskalicky.lepsirozvrh.theme;

import java.io.IOException;

public class DefaultThemes {
    public static final String LIGHT = "H4sIAAAAAAAAAH2SW2+DIBiG/wvXzaJOO+udx7hkF01rsmsmVMkqGOaWuab/fXxI6mF1EC543o/3O4QLKsOoQgHKsmTnhWij7nvJGiz7gn53SrD00sJBiOYvPdJScDI+2FpPiR+BFNe8WpgDWvMH7W4KENazJOyLESpnLG3arjepYWkmpYCoSLk62jfXEWmcZpmt72uV5XfLytdryilWJWn7OEv8JB7hag6t/mPJqvqsDtBsF/qOB/SZn8SZcapTuQ7sKTYu4xB6zCkuatpQFFxQO9QydcRlSTm8cT0v3LqKvOHyvZLikxOIC2Gj6waR1gz+lZGuRoH9YAG8lWmwM+A9JoSp3yC6TjQoeJzRF3rqluwwtDqHhWhvCFoz2GT5aKdtH9mP6tE2ymzqtj/AxbAh9voLOXid9hEDAAA=";
    public static final String DARK = "H4sIAAAAAAAAAH2SUU+DMBDHv0ufF1M6jcDbRiWY+LBsJD5X2kHjaEmtRlz23e0VsgGCXHjo73+9+9+lZ1RstiWKUZpGIcZo5c47I2tm2lx8Wy/A54W91nVPKaGPNPL0IAqt+NyFpFLlpDigpfqgzbYAYbkLlV+SC+NYQCCAPdWNbX3rATNGQ9YWY0y8mazLSCH8eclZNmsrW/aUCeYs+fLrCOIGF3t49Z+SsqxO7vc02oTkAeizOuqTVMK3uicQQ/ynSsuUYHklaoHiM2o6L8OKrCiEgjtJSkOaOPLGivfS6E/Fb+u8rBBv+sW/Sm4rFAd3GODV5hjvGOfSvQZtra5RvB7RF3G0U7bvRh3DXDdXBKP1GMUE2EczHPsgf9yMQa+Mth6EHZwsG3Ivv/6I84URAwAA";
    public static final String BLACK = "H4sIAAAAAAAAAH2SX2+DIBTFvwvPzYLWLs63+i8u2UPTmuyZCVWyCoaxZa7pdx8XjVWng/jA78C55954RcU+LFGAvEcv8lK0MeeD4jVRbc6+tRFSu6xwlLLuabJN/CS09MQKKejSg6gS5cwc0Jo/aIslQFivEvMvTpkyzHFhA0vqRre29IgpJeFWiDF2MZDM3sB22fNasmwxVraeKWPERLL2rgf7DldrWPUfS15WF/NZ+rT33R3QZ3GWFy5YN2Zba4z/uLREMJJXrGYouKKmyzJ2JEXBBLyJ0tiPI0PeSPFeKvkp6H1Ytw2iTT/4V051hQLnYQdwiDlgDPhAKOXmb5BayxoF2wl9YWc9Z8eu1SnMZTMgaK3HKHCBfTTjtk/8x/To9Mpk6o7fwdmw4e7tF5iPfB0RAwAA";

    public static ThemeData getLightTheme(){
        try {
            return ThemeData.parseZipped(LIGHT);
        } catch (IOException e) {
            //this should never happen
            return null;
        }
    }
    public static ThemeData getDarkTheme(){
        try {
            return ThemeData.parseZipped(DARK);
        } catch (IOException e) {
            //this should never happen
            return null;
        }
    }
    public static ThemeData getBlackTheme(){
        try {
            return ThemeData.parseZipped(BLACK);
        } catch (IOException e) {
            //this should never happen
            return null;
        }
    }
}

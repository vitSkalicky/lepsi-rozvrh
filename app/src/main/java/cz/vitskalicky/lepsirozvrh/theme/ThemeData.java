package cz.vitskalicky.lepsirozvrh.theme;

import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Class containing theme values. JSONable.
 * <p>
 * 'c' for color, 'dp' for dimension in dp, 'sp' for text size in sp, etc.
 * 'H' for normal lesson (stands for 'Hodina'), 'Chng' for changed, 'A' for no school (stands for 'Absence', probably. The Bakláři API just calls it so).
 * Example: cHBg = color of normal lesson background
 */
public class ThemeData {
    /**
     * the basic values such as primary, accent acolors, etc. are stored here.
     */
    @JsonSerialize(using = CyaneaThemeSerializer.class)
    @JsonDeserialize(using = CyaneaThemeDeserializer.class)
    public CyaneaTheme cyaneaTheme;

    // my addition values
    // most of them, are for cell views

    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cEmptyBg;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cABg;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHBg;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cChngBg;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHeaderBg;

    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cDivider;
    public float dpDividerWidth;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHighlight; //for current lesson
    public float dpHighlightWidth;

    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHPrimaryText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHRoomText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHSecondaryText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cChngPrimaryText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cChngRoomText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cChngSecondaryText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cAPrimaryText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cARoomText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cASecondaryText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHeaderPrimaryText;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHeaderSecondaryText;
    public float spPrimaryText;
    public float spSecondaryText;

    public float dpPaddingLeft;
    public float dpPaddingTop;
    public float dpPaddingRight;
    public float dpPaddingBottom;
    public float dpTextPadding;

    // info line
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cInfolineBg;
    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cInfolineText;
    public float spInfolineTextSize;

    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cError;

    //<editor-fold desc="Annotations" defaultstate="collapsed">
    @JsonSerialize(using = ColorSerializer.class)
    @JsonDeserialize(using = ColorDeserializer.class)
    //</editor-fold>
    public int cHomework;
    public float dpHomework;

    /**
     * Parses hexadecimal color string, with or without alpha (if no alpha supplied, 255 is used - opaque), with or without # prefix. (eg. "012830" or "0f014230" or "#0f014230")
     *
     * @param hex hexadecimal color string
     * @return color integer
     * @throws IllegalArgumentException if the color string is invalid
     */
    public static int colorHexToInt(String hex) throws IllegalArgumentException {
        hex = hex.toLowerCase();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        boolean alpha;
        if (hex.matches("[0-9a-f]{8}")) {
            alpha = true;
        } else if (hex.matches("[0-9a-f]{6}")) {
            alpha = false;
        } else {
            throw new IllegalArgumentException("Expected rgb or argb hex color string with or wothout \"#\" prefix (eg. \"012830\" or \"0f014230\" or \"#0f014230\"), but got \"" + hex + "\".");
        }
        try {
            int integerValue = new BigInteger(hex, 16).intValue();
            if (alpha) {
                return integerValue;
            } else {
                return 0xff000000 | integerValue;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected rgb or argb hex color string with or wothout \"#\" prefix (eg. \"012830\" or \"0f014230\" or \"#0f014230\"), but got \"" + hex + "\".");
        }
    }

    /**
     * Converts color int to hexadecimal string. If alpha is 255, it is omitted.
     *
     * @param color         color int
     * @param includePrefix wheter to include # on the beginning
     * @return Hexadecimal color string. eg. 0F0F0F, #424200, 90555555, #a0ffffff
     */
    public static String colorIntToHex(int color, boolean includePrefix) {
        String hex;
        if (color != ( 0xff000000 | color)) {
            hex = String.format("%08X", color);
        } else {
            hex = String.format("%06X", 0x00ffffff & color);
        }
        if (includePrefix) {
            hex = "#" + hex;
        }
        return hex;
    }

    public static class CyaneaThemeSerializer extends StdSerializer<CyaneaTheme> {

        protected CyaneaThemeSerializer() {
            super(CyaneaTheme.class);
        }

        @Override
        public void serialize(CyaneaTheme value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            JSONObject jo = new JSONObject();
            try {
                jo.put("primary", colorIntToHex(value.getPrimary(), false));
                jo.put("accent", colorIntToHex(value.getAccent(), false));
                jo.put("background", colorIntToHex(value.getBackground(), false));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            gen.writeRawValue(jo.toString());
        }
    }

    public static class CyaneaThemeDeserializer extends StdDeserializer<CyaneaTheme> {

        protected CyaneaThemeDeserializer() {
            super(CyaneaTheme.class);
        }

        @Override
        public CyaneaTheme deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            p.getText();
            try {
                TreeNode tn = p.readValueAsTree();
                Cyanea temp = Cyanea.getInstance("temp446");

                Theme.Utils.setPrimaryCorrectly(temp.edit(), colorHexToInt(tn.get("primary").toString().replace("\"", "")))
                        .accent(colorHexToInt(tn.get("accent").toString().replace("\"", "")))
                        .background(colorHexToInt(tn.get("background").toString().replace("\"", "")));
                return new CyaneaTheme("", temp);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    public static class ColorSerializer extends StdSerializer<Integer> {

        protected ColorSerializer() {
            super(Integer.class);
        }

        @Override
        public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(colorIntToHex(value, false));
        }
    }

    public static class ColorDeserializer extends StdDeserializer<Integer> {

        protected ColorDeserializer() {
            super(Integer.class);
        }

        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String text = p.getText();
            try {
                return colorHexToInt(text);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException(p, "Expected rgb or argb hex color string with or wothou \"#\" prefix (eg. \"012830\" or \"0f014230\" or \"#0f014230\"), but got \"" + text + "\".");
            }
        }
    }

    /**
     * Serializes this object using JSON and writes it to the given output stream.
     * @throws IOException if it fails
     */
    public void toJsonString(OutputStream os) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(os, this);
        } catch (IOException e) {
            throw new IOException("Failed to write ThemeData.",e);
        }
    }

    /**
     * Serializes this object using JSON.
     * @return JSON string or {@code null} if it fails (it shouldn't)
     */
    public String toJsonString(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serializes this object using JSON, compresses using gzip, encodes in base64 and writes it to the given output stream.
     * @throws IOException if it fails
     */
    public void toZippedString(@Nullable OutputStream os) throws IOException{
        Base64OutputStream bos = new Base64OutputStream(os, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        GZIPOutputStream gzos = new GZIPOutputStream(bos);
        toJsonString(gzos);
    }

    /**
     * Serializes this object using JSON, compresses using gzip and encodes in base64
     * @return JSON string or {@code null} if it fails (it shouldn't)
     */
    public String toZippedString(){
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            toZippedString(baos);
            return baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deserializes a {@link ThemeData} from JSON
     * @param is Input stream to read from.
     * @throws IOException if anything fails
     * @return Deserialized ThemeData
     */
    public static ThemeData parseJson(InputStream is) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        try{
            return mapper.readValue(is, ThemeData.class);
        } catch (IOException e) {
            throw new IOException("Failed to parse theme",e);
        }
    }

    /**
     * Deserializes a {@link ThemeData} from JSON
     * @param s Json string
     * @throws IOException if parsing fails
     * @return Deserialized ThemeData
     */
    public static ThemeData parseJson(String s) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        try{
            return mapper.readValue(s, ThemeData.class);
        } catch (IOException e) {
            throw new IOException("Failed to parse theme from this string:" + s,e);
        }
    }

    /**
     * Deserializes a {@link ThemeData} from base64 encoded (no wrap), gzipped, JSON.
     * @param is Input stream to read from.
     * @throws IOException if anything fails
     * @return Deserialized ThemeData
     */
    public static ThemeData parseZipped(InputStream is) throws IOException{
        Base64InputStream bis = new Base64InputStream(is, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        GZIPInputStream gzis = new GZIPInputStream(bis);
        return parseJson(gzis);
    }

    /**
     * Deserializes a {@link ThemeData} from base64 encoded (no wrap), gzipped, JSON.
     * @param s base64 encoded (no wrap), gzipped, JSON
     * @throws IOException if anything fails
     * @return Deserialized ThemeData
     */
    public static ThemeData parseZipped(String s) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
        return parseZipped(bais);
    }

}
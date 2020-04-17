package cz.vitskalicky.lepsirozvrh.theme;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Class containing theme values. JSONable.
 *
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

    public static class CyaneaThemeSerializer extends StdSerializer<CyaneaTheme> {

        protected CyaneaThemeSerializer() {
            super(CyaneaTheme.class);
        }

        @Override
        public void serialize(CyaneaTheme value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toJson().toString());
        }
    }

    public static class CyaneaThemeDeserializer extends StdDeserializer<CyaneaTheme> {

        protected CyaneaThemeDeserializer() {
            super(CyaneaTheme.class);
        }

        @Override
        public CyaneaTheme deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            try {
                String text = p.getText();
                return CyaneaTheme.Companion.newInstance(new JSONObject(text));
            } catch (JSONException e) {
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
            gen.writeString(String.format("#%08X",value));
        }
    }

    public static class ColorDeserializer extends StdDeserializer<Integer>{

        protected ColorDeserializer() {
            super(Integer.class);
        }

        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String text = p.getText().toLowerCase();
            if(!text.matches("#[0-9a-f]{8}")){
                throw new JsonParseException(p, "Expected #argb hex color string (eg. \"#0f420042\"), but got \"" + text + "\".");
            }
            try{
                return Integer.parseInt(p.getText().substring(1),16);
            }catch (NumberFormatException e){
                throw new JsonParseException(p, "Expected #argb hex color string (eg. \"#0f420042\"), but got \"" + text + "\".", e);
            }
        }
    }
}
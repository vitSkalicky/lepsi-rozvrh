package cz.vitskalicky.lepsirozvrh.theme;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Class containing theme values. JSONable.
 *
 * 'c' for color, 'dp' for dimension in dp, 'sp' for text size in sp, etc.
 */
public class Theme {
    /**
     * the basic values such as primary, accent acolors, etc. are stored here.
     */
    @JsonSerialize(using = CyaneaThemeSerializer.class)
    @JsonDeserialize(using = CyaneaThemeDeserializer.class)
    public CyaneaTheme cyaneaTheme;

    // my addition values
    // most of them, are for cell views

    public int cBgEmpty;
    public int cBgA;
    public int cBgH;
    public int cBgChng;
    public int cBgHeader;

    public int cDivider;
    public float dpDividerWidth;
    public int cHighlight; //for current lesson
    public float dpHighlightWidth;

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
    public float spPrimaryText;
    public float spSecondaryText;

    public float dpPaddingLeft;
    public float dpPaddingTop;
    public float dpPaddingRight;
    public float dpPaddingBottom;
    public float dpTextPadding;

    // info line
    public int cInfoline;
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
}

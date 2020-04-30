package cz.vitskalicky.lepsirozvrh.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import com.jaredrummler.cyanea.Cyanea;

import cz.vitskalicky.lepsirozvrh.theme.Theme;

public class CustomPreferenceCategory extends PreferenceCategory {
    public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Cyanea cyanea = Cyanea.getInstance();
        int textColor = cyanea.getAccent();
        if (!Theme.Utils.isLegible(cyanea.getAccent(), cyanea.getBackgroundColor(), 3)) {
            textColor = Theme.Utils.whichTextColor(cyanea.getBackgroundColor(), cyanea.getAccentDark(), cyanea.getAccentLight());
            if (!Theme.Utils.isLegible(textColor, cyanea.getBackgroundColor(), 3)) {
                textColor = Theme.Utils.textColorFor(cyanea.getBackgroundColor());
            }

        }
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        titleView.setTextColor(textColor);
    }
}

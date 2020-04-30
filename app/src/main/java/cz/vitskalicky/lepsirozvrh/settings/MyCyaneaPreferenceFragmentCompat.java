package cz.vitskalicky.lepsirozvrh.settings;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.jaredrummler.cyanea.Cyanea;

import cz.vitskalicky.lepsirozvrh.theme.Theme;

public abstract class MyCyaneaPreferenceFragmentCompat extends PreferenceFragmentCompat {

    @Override
    public void setPreferencesFromResource(int preferencesResId, @Nullable String key) {
        super.setPreferencesFromResource(preferencesResId, key);
        fixPreferenceColors(getPreferenceScreen());
    }

    protected void fixPreferenceColors(PreferenceGroup pg) {
        int iconColor = Theme.Utils.textColorFor(Cyanea.getInstance().getBackgroundColor());
        for (int i = 0; i < pg.getPreferenceCount(); i++) {
            Preference p = pg.getPreference(i);
            Drawable icon = p.getIcon();
            if (icon != null) {
                icon.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_ATOP));
                p.setIcon(icon);
            }
            if (p instanceof PreferenceGroup) {
                PreferenceGroup newpg = (PreferenceGroup) p;
                fixPreferenceColors(newpg);
            }
        }
    }
}

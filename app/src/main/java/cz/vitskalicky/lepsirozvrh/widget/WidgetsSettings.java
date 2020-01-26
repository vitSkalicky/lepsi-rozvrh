package cz.vitskalicky.lepsirozvrh.widget;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.HashSet;

public class WidgetsSettings {
    public HashSet<Integer> widgetIds;
    public HashMap<Integer, Widged> widgets;

    public static class Widged{
        public int backgroundColor;
        public int primaryTextColor;
        public int secondaryTextColor;
        public float primaryTextSize;
        public float secondaryTextSize;
    }

    @SuppressLint("UseSparseArrays")
    public WidgetsSettings() {
        widgetIds = new HashSet<>();
        widgets = new HashMap<>();
    }
}

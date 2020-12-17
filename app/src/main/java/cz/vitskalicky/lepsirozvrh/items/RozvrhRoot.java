/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.items;

import android.content.Context;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import cz.vitskalicky.lepsirozvrh.DebugUtils;

@Root(name = "results", strict = false)
public class RozvrhRoot {
    public RozvrhRoot() {
        super();
    }

    @Element(required = false)
    private OldRozvrh rozvrh;

    public OldRozvrh getRozvrh() {
        return rozvrh;
    }

    public void setRozvrh(OldRozvrh oldRozvrh) {
        this.rozvrh = oldRozvrh;
    }

    /**
     * checks if demonstration mode is enabled, if so it sets itself to demonstration Rozvrh
     */
    public void checkDemoMode(Context context){
        if (DebugUtils.getInstance(context).isDemoMode()){
            rozvrh = DebugUtils.getInstance(context).getDemoOldRozvrh();
        }
    }
}

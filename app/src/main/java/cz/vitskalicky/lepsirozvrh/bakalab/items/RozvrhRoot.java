/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.bakalab.items;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "results", strict = false)
public class RozvrhRoot {
    public RozvrhRoot() {
        super();
    }

    @Element(required = false)
    private Rozvrh rozvrh;

    public Rozvrh getRozvrh() {
        return rozvrh;
    }
}

/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.items;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "hod", strict = false)
public class RozvrhHodina {
    public RozvrhHodina() {
        super();
    }

    private String begintime = "";

    private String endtime = "";

    @Element(required = false)
    private String typ;

    @Element(required = false)
    private String zkrpr;

    @Element(required = false)
    private String pr;

    @Element(required = false)
    private String zkruc;

    @Element(required = false)
    private String uc;

    @Element(required = false)
    private String zkrmist;

    @Element(required = false)
    private String tema;

    @Element(required = false)
    private String caption;

    @Element(required = false)
    private String zkratka;

    @Element(required = false)
    private String nazev;

    private boolean expanded;

    public String getBegintime() {
        return begintime;
    }

    public void setBegintime(String begintime) {
        this.begintime = begintime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getTyp() {
        return typ;
    }

    public String getZkrpr() {
        return zkrpr;
    } //zkratka předmětu

    public String getPr() {
        return pr;
    } //předmět

    public String getZkruc() {
        return zkruc;
    } //zkratka učitele

    public String getUc() {
        return uc;
    } //učitel

    public String getZkrmist() {
        return zkrmist;
    } //zkratka místa

    public String getTema() {
        return tema;
    } //téma

    public String getCaption() {
        return caption;
    } //lesson number / číslo hodiny

    public String getZkratka() {
        return zkratka;
    }

    public String getNazev() {
        return nazev;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}

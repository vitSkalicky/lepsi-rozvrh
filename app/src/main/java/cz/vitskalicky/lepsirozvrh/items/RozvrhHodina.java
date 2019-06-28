/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.items;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

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

    @Element(required = false)
    private String zkrskup;

    @Element(required = false)
    private String chng;

    @Element(required = false)
    private String cycle;

    public static final int NONE = 0;
    public static final int CHANGED = 1;
    public static final int NO_LESSON = 2;

    private int highlight = 0;

    @Commit
    public void commit(){
        if (chng == null) chng = "";
        if (!chng.equals("")){
            highlight = CHANGED;
        }else if (typ.equals("A")){
            highlight = NO_LESSON;
        }else {
            highlight = NONE;
        }
    }

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

    public String getChng() {
        return chng;
    }

    public void setChng(String chng) {
        this.chng = chng;
    }

    public String getZkrskup() {
        return zkrskup;
    }

    public void setZkrskup(String zkrskup) {
        this.zkrskup = zkrskup;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public void setZkrpr(String zkrpr) {
        this.zkrpr = zkrpr;
    }

    public void setPr(String pr) {
        this.pr = pr;
    }

    public void setZkruc(String zkruc) {
        this.zkruc = zkruc;
    }

    public void setUc(String uc) {
        this.uc = uc;
    }

    public void setZkrmist(String zkrmist) {
        this.zkrmist = zkrmist;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setZkratka(String zkratka) {
        this.zkratka = zkratka;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public int getHighlight() {
        return highlight;
    }
}

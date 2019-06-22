/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.items;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.util.List;
import java.util.ListIterator;

@Root(name = "rozvrh", strict = false)
public class Rozvrh {

    public Rozvrh() {
        super();
    }

    @Element(required = false)
    private String typ;

    @ElementList(required = false)
    private List<RozvrhHodinaCaption> hodiny;

    @ElementList(required = false)
    private List<RozvrhDen> dny;

    @Commit
    private void onCommit(){
        deleteNullDays();
        deleteNullCaptions();
        deleteRedundantLessons();
    }

    private void deleteNullDays(){
        ListIterator<RozvrhDen> iteratorDen = dny.listIterator(0);
        while (iteratorDen.hasNext()) {
            RozvrhDen den = iteratorDen.next();
            if(den.getHodiny() == null)
                iteratorDen.remove();
        }
    }

    private void deleteNullCaptions(){
        ListIterator<RozvrhHodinaCaption> iteratorCaption = hodiny.listIterator(0);
        while (iteratorCaption.hasNext()) {
            RozvrhHodinaCaption caption = iteratorCaption.next();
            if(caption.getBegintime() == null || caption.getEndtime() == null)
                iteratorCaption.remove();
        }
    }

    private void deleteRedundantLessons(){
        //we also call fixTimes here for each day to assign begintime and endtime
        //TODO: checking for free classes at the beginning of the day in a smart way
        for(RozvrhDen den : dny){
            den.fixTimes(hodiny);
            List<RozvrhHodina> denHodiny = den.getHodiny();
            ListIterator<RozvrhHodina> i = denHodiny.listIterator(hodiny.size());
            while (i.hasPrevious()) {
                RozvrhHodina hodina = i.previous();
                if(!hodina.getTyp().equals("X"))
                    break;

                i.remove();
            }
        }
    }

    public List<RozvrhHodinaCaption> getHodiny() {
        return hodiny;
    }

    public List<RozvrhDen> getDny() {
        return dny;
    }

    public String getTyp() {
        return typ;
    }
}

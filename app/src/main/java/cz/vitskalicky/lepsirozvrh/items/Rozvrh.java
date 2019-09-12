/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.items;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
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

    @Element(required = false)
    private String nazevcyklu;

    @Element(required = false)
    private String zkratkacyklu;

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
                if(!(hodina.getHighlight() == RozvrhHodina.EMPTY))
                    break;

                i.remove();
            }
        }
    }

    /**
     * returns the lesson, which should be highlighted to the user as next or current lesson or null
     * if the school is over or this is not the current week.
     */
    public GetNLreturnValues getNextLesson(){
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        RozvrhDen dneska = null;
        int denIndex = 0;
        for (RozvrhDen item :dny) {
            if  (item.getParsedDatum() == null) //permanent timetable check
                return null;
            if (item.getParsedDatum().isEqual(nowDate)){
                dneska = item;
                break;
            }
            denIndex++;
        }

        if (dneska == null) //current timetable check
            return null;

        RozvrhHodina dalsi = null;
        int hodinaIndex = 0;
        for (int i = 0; i < dneska.getHodiny().size(); i++) {
            RozvrhHodina item = dneska.getHodiny().get(i);
            if (nowTime.isBefore(item.getParsedEndtime()) && !item.getTyp().equals("X")){
                dalsi = item;
                break;
            }
            hodinaIndex++;
        }

        if (dalsi == null) {
            denIndex = -1;
            hodinaIndex = -1;
        }

        GetNLreturnValues ret = new GetNLreturnValues();
        ret.rozvrhHodina = dalsi;
        ret.dayIndex = denIndex;
        ret.lessonIndex = hodinaIndex;


        return ret;
    }

    public static class GetNLreturnValues{
        public RozvrhHodina rozvrhHodina;
        public int dayIndex;
        public int lessonIndex;
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

    public String getNazevcyklu() {
        return nazevcyklu;
    }

    public String getZkratkacyklu() {
        return zkratkacyklu;
    }
}

/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.items;

import android.util.Log;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

@Root(name = "rozvrh", strict = false)
public class Rozvrh {
    public static final String TAG = Rozvrh.class.getSimpleName();

    public Rozvrh() {
        super();
    }

    @Element(required = false)
    private String typ = "";

    @ElementList(required = false)
    private List<RozvrhHodinaCaption> hodiny = new LinkedList<>();

    @ElementList(required = false)
    private List<RozvrhDen> dny = new LinkedList<>();

    @Element(required = false)
    private String nazevcyklu = "";

    @Element(required = false)
    private String zkratkacyklu = "";

    @Commit
    private void onCommit() {
        deleteNullDays();
        deleteNullCaptions();
        deleteRedundantLessons();
    }

    private void deleteNullDays() {
        ListIterator<RozvrhDen> iteratorDen = dny.listIterator(0);
        while (iteratorDen.hasNext()) {
            RozvrhDen den = iteratorDen.next();
            if (den.getHodiny().isEmpty())
                iteratorDen.remove();
        }
    }

    private void deleteNullCaptions() {
        ListIterator<RozvrhHodinaCaption> iteratorCaption = hodiny.listIterator(0);
        while (iteratorCaption.hasNext()) {
            RozvrhHodinaCaption caption = iteratorCaption.next();
            if (caption.getBegintime().isEmpty() || caption.getEndtime().isEmpty())
                iteratorCaption.remove();
        }
    }

    private void deleteRedundantLessons() {
        //we also call fixTimes here for each day to assign begintime and endtime
        for (RozvrhDen den : dny) {
            den.fixTimes(hodiny);
            List<RozvrhHodina> denHodiny = den.getHodiny();
            ListIterator<RozvrhHodina> i = denHodiny.listIterator(denHodiny.size());
            while (i.hasPrevious()) {
                RozvrhHodina hodina = i.previous();
                if (!(hodina.getHighlight() == RozvrhHodina.EMPTY))
                    break;

                i.remove();
            }
        }
    }

    /**
     * @see #getHighlightLesson(boolean)
     */
    public GetNLreturnValues getHighlightLesson() {
        return getHighlightLesson(false);
    }

    /**
     * returns the lesson, which should be highlighted to the user as next or current lesson, or null
     * if the school is over or this is not the current week.
     * @param forNotification If true, the first lesson won't be highlighted up until one hour before its start
     */
    public GetNLreturnValues getHighlightLesson(boolean forNotification) {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        RozvrhDen dneska = null;
        int denIndex = 0;
        for (RozvrhDen item : dny) {
            if (item.getParsedDatum() == null) //permanent timetable check
                return null;
            if (item.getParsedDatum().isEqual(nowDate)) {
                dneska = item;
                break;
            }
            denIndex++;
        }

        if (dneska == null) //current timetable check
            return null;

        RozvrhHodina dalsi = null;
        boolean prvni = true;
        int hodinaIndex = 0;
        for (int i = 0; i < dneska.getHodiny().size(); i++) {
            RozvrhHodina item = dneska.getHodiny().get(i);
            if (item.getTyp().equals("H") || !prvni){
                if (forNotification && prvni && nowTime.isBefore(item.getParsedBegintime().minusHours(1))){//do not highlight
                    return null;
                }
                if (nowTime.isBefore(item.getParsedEndtime().minusMinutes(10))) {
                    dalsi = item;
                    break;
                }
                prvni = false;
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

    public static class GetNLreturnValues {
        public RozvrhHodina rozvrhHodina;
        public int dayIndex;
        public int lessonIndex;
    }

    /**
     * Returns the time, when the notification or widget should be updated, or empty {@link GetNCLCTreturnValues} with error code if this is
     * a permanent schedule ({@link GetNCLCTreturnValues#errCode} = 1), old schedule ({@link GetNCLCTreturnValues#errCode} = 2) or there was a different problem ({@link GetNCLCTreturnValues#errCode} = 3)
     */
    public GetNCLCTreturnValues getNextCurrentLessonChangeTime() {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        int denIndex = 0;

        RozvrhDen den = null;
        if (dny.size() < 1){
            return new GetNCLCTreturnValues(null, null, 3);
        }else if (dny.get(0).getParsedDatum() == null) {
            return new GetNCLCTreturnValues(null,null,1);
        } else if (nowDate.isAfter(dny.get(dny.size() - 1).getParsedDatum())) {
            return new GetNCLCTreturnValues(null,null,2);
        } else if (nowDate.isBefore(dny.get(0).getParsedDatum())) {
            den = dny.get(0);
        } else {
            for (RozvrhDen item : dny) {
                if (item.getParsedDatum().isEqual(nowDate)) {
                    den = item;
                    break;
                }
                denIndex++;
            }
        }

        if (den == null) //current timetable check
            return new GetNCLCTreturnValues(null,null,2);

        if (nowDate.isBefore(den.getParsedDatum())){
            nowTime = LocalTime.fromMillisOfDay(0);
        }

        LocalTime cas = null;
        RozvrhHodina hodina = null;
        boolean prvni = true;
        for (int i = 0; i < den.getHodiny().size(); i++) {
            RozvrhHodina item = den.getHodiny().get(i);
            if (!item.isEmpty()){
                if (prvni && nowTime.isBefore(item.getParsedBegintime().minusHours(1))){
                    cas = item.getParsedBegintime().minusHours(1);
                    hodina = item;
                    break;
                } else if (nowTime.isBefore(item.getParsedEndtime().minusMinutes(10))) {
                    cas = item.getParsedEndtime().minusMinutes(10);
                    hodina = item;
                    break;
                }
                prvni = false;
            }

        }

        for (int i = 1; cas == null; i++) {
            //after school
            if (denIndex >= dny.size() - i){
                return new GetNCLCTreturnValues(null,null,2);
            }
            den = dny.get(denIndex + i);
            nowTime = LocalTime.fromMillisOfDay(0);

            for (int j = 0; j < den.getHodiny().size(); j++) {
                RozvrhHodina item = den.getHodiny().get(j);
                if (item.getTyp().equals("H")) {
                    cas = item.getParsedBegintime().minusHours(1);
                    hodina = item;
                    break;
                }
            }
        }

        LocalDateTime dateTime = den.getParsedDatum().toLocalDateTime(cas);

        GetNCLCTreturnValues ret = new GetNCLCTreturnValues();
        ret.rozvrhHodina = hodina;
        ret.localDateTime = dateTime;
        ret.errCode = 0;

        return ret;
    }

    public static class GetNCLCTreturnValues {
        public RozvrhHodina rozvrhHodina;
        public LocalDateTime localDateTime;
        /**
         * 0 not error; 1 permanent schedule; 2 old schedule; 3 other
         */
        public int errCode;

        public GetNCLCTreturnValues() {
        }

        public GetNCLCTreturnValues(RozvrhHodina rozvrhHodina, LocalDateTime localDateTime, int errCode) {
            this.rozvrhHodina = rozvrhHodina;
            this.localDateTime = localDateTime;
            this.errCode = errCode;
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

    public String getNazevcyklu() {
        return nazevcyklu;
    }

    public String getZkratkacyklu() {
        return zkratkacyklu;
    }

    /**
     * Return a description of of rozvrh's structure. Used for crash reports.
     * <p>
     * structure:
     * typ; zkratkacyklu;
     * captions count; first caption start time; last caption end time;
     * //for each day one line
     * zkratka; lesson count; first lesson caption; first lesson begin time; last caption; last end time;
     */
    public String getStructure() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append(typ).append("; ")
                    .append(nazevcyklu).append(";\n")
                    .append(hodiny.size()).append("; ")
                    .append(hodiny.get(0).getBegintime()).append("; ")
                    .append(hodiny.get(hodiny.size() - 1).getEndtime()).append(";\n");
            for (RozvrhDen item : dny) {
                sb.append(item.getZkratka()).append("; ")
                        .append(item.getHodiny().size()).append("; ")
                        .append(item.getHodiny().get(0).getCaption()).append("; ")
                        .append(item.getHodiny().get(0).getBegintime()).append("; ")
                        .append(item.getHodiny().get(item.getHodiny().size() - 1).getCaption()).append("; ")
                        .append(item.getHodiny().get(item.getHodiny().size() - 1).getEndtime()).append(";\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "Creating rozvrh structure failed", e);
        }
        return sb.toString();
    }
}

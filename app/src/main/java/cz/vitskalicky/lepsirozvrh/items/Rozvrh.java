/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.items;

import android.content.Context;
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

import cz.vitskalicky.lepsirozvrh.DebugUtils;
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI;

@Root(name = "rozvrh", strict = false)
public class Rozvrh {
    public static final String TAG = Rozvrh.class.getSimpleName();
    @Element(required = false)
    protected String typ = "";
    @ElementList(required = false)
    protected List<RozvrhHodinaCaption> hodiny = new LinkedList<>();
    @ElementList(required = false)
    protected List<RozvrhDen> dny = new LinkedList<>();
    @Element(required = false)
    protected String nazevcyklu = "";
    @Element(required = false)
    protected String zkratkacyklu = "";

    public Rozvrh() {
        super();
    }

    @Commit
    public void onCommit() {
        deleteNullDays();
        deleteNullCaptions();
        fillEmptyLessons();
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
    private void fillEmptyLessons(){
        for (RozvrhDen den :dny) {
            LinkedList<RozvrhHodina> newHodiny = new LinkedList();
            int hodinaIndex = 0;
            int captionIndex = 0;
            boolean lastOk = false;
            while (hodinaIndex < den.getHodiny().size() && captionIndex < hodiny.size()){
                String captionId = (hodiny.get(captionIndex).getCaption());
                String hodinaCaptionId = den.getHodiny().get(hodinaIndex).getCaption();

                if (captionId.equals(hodinaCaptionId)){
                    newHodiny.add(den.getHodiny().get(hodinaIndex));
                    hodinaIndex++;
                    lastOk = true;
                }else if(lastOk){
                    lastOk = false;
                    captionIndex++;
                }else {
                    RozvrhHodina empty = new RozvrhHodina();
                    empty.setTyp("X");
                    empty.setUkolodevzdat("");
                    empty.setNotice("");
                    empty.setSkup("");
                    empty.setAbs("");
                    empty.setMist("");
                    empty.setChng("");
                    empty.setZkrskup("");
                    empty.setZkrpr("");
                    empty.setPr("");
                    empty.setZkruc("");
                    empty.setUc("");
                    empty.setZkrmist("");
                    empty.setTema("");
                    empty.setCaption(captionId);
                    empty.setZkratka("");
                    empty.setNazev("");
                    empty.setCycle("");
                    empty.commit();

                    newHodiny.add(empty);
                    captionIndex++;
                }
            }

            den.setHodiny(newHodiny);
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
        // remove excess empty lessons at the start and end of each day (empty 0th lessons,..)
        // todo: test o some more real schedules (it is holiday by the time I am developing this)

        boolean[] removable = new boolean[getHodiny().size()];

        for (int i = 0; i < getHodiny().size(); i++) {
            boolean empty = true;
            for (int j = 0; j < getDny().size(); j++) {
                if (i < getDny().get(j).getHodiny().size()) {
                    RozvrhHodina hodina = getDny().get(j).getHodiny().get(i);
                    empty = empty && (hodina == null);
                }
            }
            removable[i] = empty;
        }

        int removeStart = 0;
        for (int i = 0; i < removable.length; i++) {
            if (removable[i]) {
                removeStart++;
            } else {
                break;
            }
        }
        int removeEnd = 0;
        for (int i = removable.length - 1; i >= 0; i--) {
            if (removable[i]) {
                removeEnd++;
            } else {
                break;
            }
        }

        //It might happen, that the whole week is holiday. I that case we don't want to remove anything.
        if (removeEnd + removeStart < removable.length) {
            for (int i = 0; i < removeStart; i++) {
                getHodiny().remove(0);
                for (RozvrhDen item : getDny()) {
                    if (item.getHodiny().size() > 0) {
                        item.getHodiny().remove(0);
                    }
                }
            }
            //we want to leave 1 empty lessons at the end
            for (int i = 0; i < removeEnd - 1; i++) {
                int lessonIndex = getHodiny().size() - 1;
                getHodiny().remove(lessonIndex);
                for (RozvrhDen item : getDny()) {
                    if (lessonIndex < item.getHodiny().size())
                    item.getHodiny().remove(lessonIndex);
                }
            }

        }
    }

    /**
     * @see #getHighlightLesson(boolean, Context)
     */
    public GetNLreturnValues getHighlightLesson(Context context) {
        return getHighlightLesson(false, context);
    }

    /**
     * returns the lesson, which should be highlighted to the user as next or current lesson, or null
     * if the school is over or this is not the current week.
     *
     * @param forNotification If true, the first lesson won't be highlighted up until one hour before its start
     */
    public GetNLreturnValues getHighlightLesson(boolean forNotification, Context context) {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        if (DebugUtils.getInstance(context).isDemoMode()) {
            nowDate = DebugUtils.getInstance(context).getDemoDate();
            nowTime = DebugUtils.getInstance(context).getDemoTime();
        }

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
            if (item.getTyp().equals("H") || !prvni) {
                if (forNotification && prvni && nowTime.isBefore(item.getParsedBegintime().minusHours(1))) {//do not highlight
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

    /**
     * Prefer {@link cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI#getNextCurrentLessonChangeTime(RozvrhAPI.TimeListener)} because that one also checks the next week if the time cannot be determine from the current one.
     * <p>
     * Returns the time, when the notification or widget should be updated, or empty {@link GetNCLCTreturnValues} with error code if this is
     * a permanent schedule ({@link GetNCLCTreturnValues#errCode} = 1), old schedule ({@link GetNCLCTreturnValues#errCode} = 2) or there was a different problem ({@link GetNCLCTreturnValues#errCode} = 3)
     */
    public GetNCLCTreturnValues getNextCurrentLessonChangeTime() {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        int denIndex = 0;

        RozvrhDen den = null;
        if (dny.size() < 1) {
            return new GetNCLCTreturnValues(null, null, 3);
        } else if (dny.get(0).getParsedDatum() == null) {
            return new GetNCLCTreturnValues(null, null, 1);
        } else if (nowDate.isAfter(dny.get(dny.size() - 1).getParsedDatum())) {
            return new GetNCLCTreturnValues(null, null, 2);
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
            return new GetNCLCTreturnValues(null, null, 2);

        if (nowDate.isBefore(den.getParsedDatum())) {
            nowTime = LocalTime.fromMillisOfDay(0);
        }

        LocalTime cas = null;
        RozvrhHodina hodina = null;
        boolean prvni = true;
        for (int i = 0; i < den.getHodiny().size(); i++) {
            RozvrhHodina item = den.getHodiny().get(i);
            if (!item.isEmpty()) {
                if (prvni && nowTime.isBefore(item.getParsedBegintime().minusHours(1))) {
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
            if (denIndex >= dny.size() - i) {
                return new GetNCLCTreturnValues(null, null, 2);
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

    /**
     * Returns lessons that should be displayed on a widget.
     *
     * @param lenght how many lessons does the widget display - determines the length of the returned array.
     * @return {@code null} if this is not a current week or it is not school-time now.
     */
    public RozvrhHodina[] getWidgetDiaplayValues(int lenght, Context context) {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        if (DebugUtils.getInstance(context).isDemoMode()) {
            nowDate = DebugUtils.getInstance(context).getDemoDate();
            nowTime = DebugUtils.getInstance(context).getDemoTime();
        }

        RozvrhDen dneska = null;
        for (RozvrhDen item : dny) {
            if (item.getParsedDatum() == null) //permanent timetable check
                return null;
            if (item.getParsedDatum().isEqual(nowDate)) {
                dneska = item;
                break;
            }
        }

        if (dneska == null) //current timetable check
            return null;

        RozvrhHodina[] ret = new RozvrhHodina[lenght];

        boolean prvni = true;
        int currentHodinaIndex = 0;
        for (int i = 0; i < dneska.getHodiny().size(); i++) {
            RozvrhHodina item = dneska.getHodiny().get(i);
            if (!item.getTyp().equals("X") || !prvni) {
                if (prvni && nowTime.isBefore(item.getParsedBegintime().minusHours(3))) {
                    return null;
                }
                if (nowTime.isBefore(item.getParsedEndtime().minusMinutes(10))) {
                    break;
                }
                prvni = false;
            }
            currentHodinaIndex++;
        }

        for (int i = 0; i < lenght && i + currentHodinaIndex < dneska.getHodiny().size(); i++) {
            ret[i] = dneska.getHodiny().get(i + currentHodinaIndex);
        }

        return ret;
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
                        .append(item.getHodiny().size()).append("; ");
                if (item.getHodiny().size() > 0) {
                    sb.append(item.getHodiny().get(0).getCaption()).append("; ")
                            .append(item.getHodiny().get(0).getBegintime()).append("; ")
                            .append(item.getHodiny().get(item.getHodiny().size() - 1).getCaption()).append("; ")
                            .append(item.getHodiny().get(item.getHodiny().size() - 1).getEndtime()).append(";\n");
                } else {
                    sb.append("empty;\n");
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Creating rozvrh structure failed", e);
        }
        return sb.toString();
    }

    public static class GetNLreturnValues {
        public RozvrhHodina rozvrhHodina;
        public int dayIndex;
        public int lessonIndex;
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

    public static class MutableRozvrh extends Rozvrh{

        public void setTyp(String typ) {
            this.typ = typ;
        }

        public void setHodiny(List<RozvrhHodinaCaption> hodiny) {
            this.hodiny = hodiny;
        }

        public void setDny(List<RozvrhDen> dny) {
            this.dny = dny;
        }

        public void setNazevcyklu(String nazevcyklu) {
            this.nazevcyklu = nazevcyklu;
        }

        public void setZkratkacyklu(String zkratkacyklu) {
            this.zkratkacyklu = zkratkacyklu;
        }


        public Rozvrh build(){
            return (Rozvrh)this;
        }
    }
}

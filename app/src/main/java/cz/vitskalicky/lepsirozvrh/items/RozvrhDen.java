/*
 Taken from Bakalab <https://github.com/bakalaborg/bakalab>
 Modified by Vít Skalický 2019
*/
package cz.vitskalicky.lepsirozvrh.items;

import android.util.Log;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import cz.vitskalicky.lepsirozvrh.Utils;
import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;

@Root(name = "den", strict = false)
public class RozvrhDen {
    public static final String TAG = RozvrhDen.class.getSimpleName();

    public RozvrhDen() {
        super();
    }

    @Element(required = false)
    private String zkratka = "";

    @Element(required = false)
    private String datum = "";

    @ElementList(required = false)
    private List<RozvrhHodina> hodiny = new LinkedList<>();

    public String getZkratka() {
        return zkratka;
    }

    public String getDatum() {
        return datum;
    }

    public LocalDate getParsedDatum(){
        if (datum == null || datum.isEmpty())
            return null;
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
        try{
            return formatter.parseLocalDate(datum);
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    public String getDay() { return Utils.parseDate(datum, "yyyyMMdd", "d"); }

    public List<RozvrhHodina> getHodiny() { return hodiny; }

    public void fixTimes(List<RozvrhHodinaCaption> captionsList) {
        int position = 0;
        RozvrhHodinaCaption mRozvrhHodinaCaption = null;
        for(RozvrhHodina hodina : hodiny){
            // V jednom caption může být víc hodin (ve stálém rozvrhu - něco sudý týden, něco jiného lichý)
            if (mRozvrhHodinaCaption == null || hodina.getCaption() == null || hodina.getCaption().isEmpty() || !mRozvrhHodinaCaption.getCaption().equals(hodina.getCaption())){
                if (position >= captionsList.size()){
                    //I've seen a weird schedule where there were more lessons than captions (lessons were outside of any caption)
                    //this is a fail-safe
                    //I simply make up some extra captions
                    Log.w(TAG, "Schedule is having more lessons than there are captions");
                    Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder().setMessage("Schedule is having more lessons than there are captions").build());

                    RozvrhHodinaCaption caption = new RozvrhHodinaCaption();
                    RozvrhHodinaCaption lastCaption = captionsList.get(captionsList.size() - 1);
                    int lastCaptionNumber = -1;
                    try {
                        lastCaptionNumber = Integer.parseInt(lastCaption.getCaption());
                    }catch (NumberFormatException ignored){}

                    DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");

                    LocalTime lastStart = LocalTime.parse(lastCaption.getBegintime(), formatter);
                    LocalTime lastEnd = LocalTime.parse(lastCaption.getEndtime(), formatter);
                    LocalTime lastLastEnd = LocalTime.parse(captionsList.get(captionsList.size() - 2).getEndtime(), formatter);

                    LocalTime thisStart = lastEnd.plusMillis(lastStart.getMillisOfDay() - lastLastEnd.getMillisOfDay());
                    LocalTime thisEnd = thisStart.plusMillis(lastEnd.getMillisOfDay() - lastStart.getMillisOfDay());

                    caption.setCaption(Integer.toString(lastCaptionNumber + 1));
                    caption.setBegintime(formatter.print(thisStart));
                    caption.setEndtime(formatter.print(thisEnd));
                    captionsList.add(caption);
                }
                mRozvrhHodinaCaption = captionsList.get(position);
                position++;
            }

            hodina.setBegintime(mRozvrhHodinaCaption.getBegintime());
            hodina.setEndtime(mRozvrhHodinaCaption.getEndtime());
        }
    }

    public int getCurrentLessonInt(){
        String currentTime = new SimpleDateFormat("H:m", Locale.US).format(new Date());
        ListIterator<RozvrhHodina> i = hodiny.listIterator();
        while (i.hasNext()) {
            if (Utils.minutesOfDay(i.next().getBegintime()) > Utils.minutesOfDay(currentTime))
                break;
        }
        return i.nextIndex();
    }
}

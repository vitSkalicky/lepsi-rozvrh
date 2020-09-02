package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3;

import android.content.Context;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;

import cz.vitskalicky.lepsirozvrh.R;
import cz.vitskalicky.lepsirozvrh.items.Rozvrh;
import cz.vitskalicky.lepsirozvrh.items.RozvrhDen;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodina;
import cz.vitskalicky.lepsirozvrh.items.RozvrhHodinaCaption;
import cz.vitskalicky.lepsirozvrh.items.RozvrhRoot;

public class RozvrhConverter {
    public static RozvrhRoot convert(Rozvrh3 rozvrh3, boolean perm, Context context){
        Rozvrh.MutableRozvrh rozvrh = new Rozvrh.MutableRozvrh();
        rozvrh.setTyp(perm ? "perm" : "akt");

        rozvrh.setNazevcyklu(rozvrh3.cycles.length > 0 ? rozvrh3.cycles[0].name : "");
        rozvrh.setZkratkacyklu(rozvrh3.cycles.length > 0 ? rozvrh3.cycles[0].abbrev : "");

        ArrayList<RozvrhHodinaCaption> captions = new ArrayList<>();
        for (Hour3 item :rozvrh3.hours) {
            RozvrhHodinaCaption nev = new RozvrhHodinaCaption();
            nev.setCaption(item.caption);
            nev.setBegintime(item.beginTime);
            nev.setEndtime(item.endTime);
            captions.add(nev);
        }
        rozvrh.setHodiny(captions);

        HashMap<String, Hour3> hours = new HashMap<>();
        for (Hour3 item :rozvrh3.hours) {
            hours.put(item.id + "", item);
        }
        HashMap<String, Class3> classes = new HashMap<>();
        for (Class3 item :rozvrh3.classes) {
            classes.put(item.id, item);
        }
        HashMap<String, Group3> groups = new HashMap<>();
        for (Group3 item :rozvrh3.groups) {
            groups.put(item.id, item);
        }
        HashMap<String, Subject3> subjects = new HashMap<>();
        for (Subject3 item :rozvrh3.subjects) {
            subjects.put(item.id, item);
        }
        HashMap<String, Teacher3> teachers = new HashMap<>();
        for (Teacher3 item :rozvrh3.teachers) {
            teachers.put(item.id, item);
        }
        HashMap<String, Room3> rooms = new HashMap<>();
        for (Room3 item :rozvrh3.rooms) {
            rooms.put(item.id, item);
        }
        HashMap<String, Cycle3> cycles = new HashMap<>();
        for (Cycle3 item :rozvrh3.cycles) {
            cycles.put(item.id, item);
        }

        String[] daysOfWeek = {
                context.getString(R.string.monday),
                context.getString(R.string.tuesday),
                context.getString(R.string.wednesday),
                context.getString(R.string.thursday),
                context.getString(R.string.friday),
                context.getString(R.string.saturday),
                context.getString(R.string.sunday)
        };

        ArrayList<RozvrhDen> days = new ArrayList<>();
        for (Day3 item :rozvrh3.days) {
            RozvrhDen newDen = new RozvrhDen();
            LocalDate date = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").parseLocalDate(item.date);
            newDen.setDatum(date.toString(RozvrhDen.DATE_FORMATTER));

            newDen.setZkratka(daysOfWeek[item.dayOfWeek - 1]);

            ArrayList<RozvrhHodina> lessons = new ArrayList<>();
            for (Atom3 atom :item.atoms) {
                RozvrhHodina newHodina = new RozvrhHodina();
                newHodina.setCaption(hours.get(atom.hourId).caption);
                newHodina.setTyp(atom.subjectId == null ? "X" : "H");
                newHodina.setZkrpr(atom.subjectId != null ? subjects.get(atom.subjectId).abbrev : "");
                newHodina.setPr(atom.subjectId != null ? subjects.get(atom.subjectId).name : "");
                newHodina.setZkruc(atom.teacherId != null ? teachers.get(atom.teacherId).abbrev : "");
                newHodina.setUc(atom.teacherId != null ? teachers.get(atom.teacherId).name : "");
                newHodina.setZkrmist(atom.roomId != null ? rooms.get(atom.roomId).abbrev : "");
                newHodina.setMist(atom.roomId != null ? rooms.get(atom.roomId).name : "");
                newHodina.setAbs(""); //ignored
                newHodina.setTema(atom.theme);

                StringBuilder zkrSkupSb = new StringBuilder();
                StringBuilder skupSb = new StringBuilder();
                for (int i = 0; i < atom.groupIds.length; i++) {
                    if (i > 0){
                        zkrSkupSb.append(", ");
                        skupSb.append(", ");
                    }
                    zkrSkupSb.append(groups.get(atom.groupIds[i]).abbrev);
                    skupSb.append(groups.get(atom.groupIds[i]).name);
                }
                newHodina.setZkrskup(zkrSkupSb.toString());
                newHodina.setZkrskup(skupSb.toString());

                StringBuilder cycleSb = new StringBuilder();
                for (int i = 0; i < atom.cycleIds.length; i++) {
                    cycleSb.append(cycles.get(atom.cycleIds[i]).abbrev);
                }
                newHodina.setCycle(cycleSb.toString());

                newHodina.setChng(atom.change == null ? "" : atom.change.description);
                if (atom.change != null){
                    if (newHodina.getZkrpr().isEmpty()){
                        newHodina.setZkrpr(atom.change.typeAbbrev);
                    }
                    if (newHodina.getPr().isEmpty()){
                        newHodina.setPr(atom.change.typeName);
                    }
                    if (atom.change.changeType.equals("Canceled")){
                        newHodina.setTyp("A");
                    }
                }

                newHodina.setUkolodevzdat(atom.homeworkIds.length > 0 ? Integer.toString(atom.homeworkIds.length) : "");

                newHodina.commit();

                lessons.add(newHodina);
            }
            newDen.setHodiny(lessons);
            days.add(newDen);
        }

        rozvrh.setDny(days);
        rozvrh.onCommit();
        RozvrhRoot rozvrhRoot = new RozvrhRoot();
        rozvrhRoot.setRozvrh(rozvrh);

        return rozvrhRoot;
    }
}

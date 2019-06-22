package cz.vitskalicky.lepsirozvrh.item;

public class RozvrhLesson {
    String id;
    String type; // H - lesson, X - empty, A - special? (school trip,...)
    String subjectShort;
    String subject;
    String teacherShort;
    String teacher;
    String roomShort;
    String room;
    String topic;
    String groupShort;
    String group;
    String cycle;
    String change;
    RozvrhLessonCaption caption;

    //<editor-fold desc="Getters">

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSubjectShort() {
        return subjectShort;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacherShort() {
        return teacherShort;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getRoomShort() {
        return roomShort;
    }

    public String getRoom() {
        return room;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroupShort() {
        return groupShort;
    }

    public String getGroup() {
        return group;
    }

    public String getCycle() {
        return cycle;
    }

    public String getChange() {
        return change;
    }

    public boolean isChanged() {
        return !change.equals("");
    }

    public RozvrhLessonCaption getCaption() {
        return caption;
    }

    //</editor-fold>


    public RozvrhLesson(String id, String type, String subjectShort, String subject, String teacherShort, String teacher, String roomShort, String room, String topic, String groupShort, String group, String cycle, String change, RozvrhLessonCaption caption) {
        this.id = id;
        this.type = type;
        this.subjectShort = subjectShort;
        this.subject = subject;
        this.teacherShort = teacherShort;
        this.teacher = teacher;
        this.roomShort = roomShort;
        this.room = room;
        this.topic = topic;
        this.groupShort = groupShort;
        this.group = group;
        this.cycle = cycle;
        this.change = change;
        this.caption = caption;
    }
}

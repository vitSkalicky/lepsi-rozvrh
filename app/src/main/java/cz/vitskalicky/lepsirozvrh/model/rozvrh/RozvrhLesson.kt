package cz.vitskalicky.lepsirozvrh.model.rozvrh

data class RozvrhLesson (
        val subjectName: String,
        val subjectAbbrev: String,
        val teacherName: String,
        val teacherAbbrev: String,
        val roomName: String,
        val roomAbbrev: String,
        val groups: List<RozvrhGroup>,
        val cycles: List<RozvrhCycle>,
        val homeworkIds: List<String>,
        val theme: String,
        /**
         * One of [NO_CHANGE], [CHANGED] od [CANCELLED]
         */
        val changeType: Int,
        /**
         * is `null` if [changeType] == [NO_CHANGE]
         */
        val changeDescription: String?


){
    companion object{
        const val NO_CHANGE = 0;

        /**
         * Lesson is moved, added, replaced, in different room, etc.
         */
        const val CHANGED = 1;

        /**
         * Lesson is cancelled due to technical problems, school goes to cinema, etc.
         */
        const val CANCELLED = 2;
    }
}
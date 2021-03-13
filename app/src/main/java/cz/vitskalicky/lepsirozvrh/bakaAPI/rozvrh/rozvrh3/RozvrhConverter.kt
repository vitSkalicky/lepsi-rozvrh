package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3

import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.model.relations.BlockRelated
import cz.vitskalicky.lepsirozvrh.model.relations.DayRelated
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object RozvrhConverter {
    @Throws(RozvrhConversionException::class)
    fun convert(rozvrh3: Rozvrh3, date: LocalDate?): RozvrhRelated{
        //todo perform further testing after creating a testing server
        val rozvrh3 = remove0thCaptionIfUnnecessary(rozvrh3)

        val monday : LocalDate = date?.let { Utils.getWeekMonday(date) } ?: Rozvrh.PERM
        val cycle: RozvrhCycle? = if (date == null){
                null
            }else{
                if (rozvrh3.cycles.isEmpty()){
                    RozvrhCycle("","","")
                }else{
                    val c3 = rozvrh3.cycles[0]
                    RozvrhCycle(c3.id, c3.name, c3.abbrev)
                }
            }

        val rozvrh = Rozvrh(monday, DateTime.now(), monday == Rozvrh.PERM, cycle)

        //caption3 id and corresponding RozvrhCaption
        val captionsUnsorted = ArrayList<Pair<String,RozvrhCaption>>()
        for (value in rozvrh3.hours.withIndex()) {
            val item = value.value
            val nev = RozvrhCaption(
                    monday,
                    monday.toString() + "-" + (item.id.hashCode() * item.beginTime.hashCode()).hashCode().toString(16),
                    item.caption,
                    LocalTime.parse(item.beginTime),
                    LocalTime.parse(item.endTime),
                    value.index
            )
            captionsUnsorted.add(Pair(item.id.toString(), nev))
        }

        //to be extra sure, we sort the caption ascending by begin time to make sure it has the right index
        captionsUnsorted.sortWith( compareBy { it.second.beginTime } )
        //here we have the RozvrhCaptions
        val captionsMap = HashMap<String, RozvrhCaption>()
        captionsUnsorted.forEachIndexed { index, pair -> captionsMap[pair.first] = pair.second.copy(index = index) }
        val captions: List<RozvrhCaption> = captionsUnsorted.mapIndexed { index, pair -> pair.second.copy(index = index) }

        val hours = HashMap<String, Hour3>()
        for (item in rozvrh3.hours) {
            hours[item.id.toString() + ""] = item
        }
        val classes = HashMap<String, Class3>()
        for (item in rozvrh3.classes) {
            classes[item.id] = item
        }
        val groups = HashMap<String, Group3>()
        for (item in rozvrh3.groups) {
            groups[item.id] = item
        }
        val subjects = HashMap<String, Subject3>()
        for (item in rozvrh3.subjects) {
            subjects[item.id] = item
        }
        val teachers = HashMap<String, Teacher3>()
        for (item in rozvrh3.teachers) {
            teachers[item.id] = item
        }
        val rooms = HashMap<String, Room3>()
        for (item in rozvrh3.rooms) {
            rooms[item.id] = item
        }
        val cycles = HashMap<String, Cycle3>()
        for (item in rozvrh3.cycles) {
            cycles[item.id] = item
        }

        val days = ArrayList<DayRelated>()

        for (item in rozvrh3.days) {

            var dayDate : LocalDate = if (monday != Rozvrh.PERM) {
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").parseLocalDate(item.date)
            }else{
                Rozvrh.PERM.plusDays(item.dayOfWeek - 1)
            }
            val event: String? = if (item.dayDescription?.isNotBlank() == true){
                    item.dayDescription
                }else{
                    null
                }

            val day = RozvrhDay(dayDate, monday, event)

            val lessons = Array<ArrayList<RozvrhLesson>>(captions.size) { ArrayList() }
            for (atom in item.atoms) {
                val caption: RozvrhCaption = captionsMap[atom.hourId] ?:
                //report problem
                throw RozvrhConversionException("Failed to parse Rozvrh3 to Rozvrh: Could not find a caption for an atom: searched for '${atom.hourId}' available caption ids: ${captionsMap.keys}")

                val captionId: String = caption.id

                var subjectName: String = ""
                var subjectAbbrev: String = ""

                atom.subjectId?.let { subjects[it] }?.let{
                    subjectName = it.name ?: ""
                    subjectAbbrev = it.abbrev ?: ""
                }

                var teacherName: String = ""
                var teacherAbbrev: String = ""

                atom.teacherId?.let { teachers[it] }?.let {
                    teacherName = it.name
                    teacherAbbrev = it.abbrev
                }

                var roomName: String = ""
                var roomAbbrev: String = ""

                atom.roomId?.let { rooms[it] }?.let {
                    roomName = it.name
                    roomAbbrev = it.abbrev
                }

                val theme = atom.theme ?: ""

                var changeType: Int = RozvrhLesson.NO_CHANGE
                var chngDesc: String? = null
                if (atom.change != null) {
                    chngDesc = atom.change.description
                    changeType = RozvrhLesson.CHANGED
                    if (!atom.change.typeAbbrev.isNullOrBlank()) {
                        changeType = RozvrhLesson.CANCELLED
                        subjectAbbrev = atom.change.typeAbbrev
                        subjectName = atom.change.typeName ?: ""
                    }
                }

                val lessonGroups = ArrayList<RozvrhGroup>()
                atom.groupIds?.forEach {
                    groups[it]?.let{
                        lessonGroups.add(RozvrhGroup(it.id, it.name, it.abbrev))
                    }
                }

                val lessonCycles = ArrayList<RozvrhCycle>()
                atom.cycleIds?.forEach {
                    cycles[it]?.let {
                        lessonCycles.add(RozvrhCycle(it.id, it.name, it.abbrev))
                    }
                }

                val homeworkIds = ArrayList<String>()
                atom.homeworkIds?.map {
                    if (it.length > 3 && atom.groupIds != null){
                        val id = it.substring(2, 4)
                        for (grp in atom.groupIds) {
                            if (grp == id) {
                                homeworkIds.add(it)
                                break
                            }
                        }
                    }
                }

                lessons[caption.index].add(RozvrhLesson(
                        subjectName,
                        subjectAbbrev,
                        teacherName,
                        teacherAbbrev,
                        roomName,
                        roomAbbrev,
                        lessonGroups,
                        lessonCycles,
                        homeworkIds,
                        theme,
                        changeType,
                        chngDesc
                ))
            }

            val blocks = ArrayList<BlockRelated>(captions.size)

            for (capt in captions){
                blocks.add(BlockRelated(
                        RozvrhBlock(
                            dayDate,
                            capt.id,
                            lessons[capt.index]
                        ),
                        capt
                    )
                )
            }
            days.add(DayRelated(day, blocks))
        }
        
        return RozvrhRelated(rozvrh, captions, days)
    }

    /**
     * Romeves 0th caption if present and if unnecessary and return a modified copy.
     */
    fun remove0thCaptionIfUnnecessary(rozvrh3: Rozvrh3): Rozvrh3{
        val zeroCaptions = rozvrh3.hours.filter { it.caption.trim() == "0" }
        if (zeroCaptions.size != 1 ){
            return rozvrh3
        }
        val zeroCaption = zeroCaptions[0]

        var isEmpty: Boolean = true;
        rozvrh3.days.forEach {
            isEmpty = isEmpty && it.atoms.none { it.hourId == zeroCaption.id.toString() }
        }
        if (!isEmpty){
            return rozvrh3
        }
        return rozvrh3.copy(hours = rozvrh3.hours.toMutableList().apply { removeAll{ it.id == zeroCaption.id }})
    }

    class RozvrhConversionException(message: String): RuntimeException(message)
}
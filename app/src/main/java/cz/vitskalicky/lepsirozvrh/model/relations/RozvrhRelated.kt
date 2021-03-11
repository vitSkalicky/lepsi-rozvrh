package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

data class RozvrhRelated(
    @Embedded val rozvrh: Rozvrh,

    @Relation(
        parentColumn = "id",
        entityColumn = "rozvrh",
        entity = RozvrhCaption::class
    )
    /**
     * captions ordered by beginTime (therefore [RozvrhCaption.index] corresponds with the index in this list)
     */
    val captions: List<RozvrhCaption>,

    @Relation(
        parentColumn = "id",
        entityColumn = "rozvrh",
        entity = RozvrhDay::class
    )
    /**
     * Day ordered by date. May not begin with monday.
     */
    val days: List<DayRelated>
) {

    /**
     * returns the lesson block, which should be highlighted to the user as next or current lesson, or null
     * if the school is over or this is not the week.
     *
     * @param forNotification If true, the first lesson won't be highlighted up until one hour before its start
     */
    fun getHighlightBlock(forNotification: Boolean): BlockRelated? {
        return days.firstOrNull { it.day.date == LocalDate.now() }?.getHighlightBlock(forNotification)
    }

    /**
     * Returns lessons that should be displayed on a widget or an empty list if all the lessons are already over. If there is en event on the current day ([RozvrhDay.event] != null),
     * the list is `null` and the string contains name of the event. Otherwise the string is `null`.
     *
     * If this is not the current week, the pair is `null`.
     *
     * @param length how many lessons does the widget display - determines the length of the returned list.
     * @return a [Pair] of nullable list and nullable string or `null` if this is not the current week.
     * The first parameter is list of lessons which should be displayed or empty list if all the lessons
     * are already over or `null` if there is an event on that day. The second parameter is the description of current event or `null`
     * if there is no event on that day.
     */
    fun getWidgetDisplayBlocks(length: Int): Pair<List<BlockRelated>?,String?>?{
        if (Utils.getCurrentMonday() != rozvrh.id){
            return null
        }
        return days.firstOrNull { it.day.date == LocalDate.now() }?.getWidgetDisplayBlocks(length) ?: Pair(emptyList(), null)
    }

    /**
     * Return time when the notification and widget should be updated or `null` if this week is already over.
     */
    fun getUpdateDisplayedDataTime(): LocalDateTime?{
        val nowDate = LocalDate.now()
        val nowTime = LocalTime.now()

        var updateTime: LocalDateTime? = null;

        val futureDays: List<DayRelated> = days.filter { !it.day.date.isBefore(nowDate) }
        var index = 0;

        while (updateTime == null && index < futureDays.size) {

            val day: DayRelated = futureDays[index]

            var first = true

            for (i in day.blocks.indices) {
                val item: BlockRelated = day.blocks[i]
                val lesson: RozvrhLesson? = item.block.lessons.getOrNull(0)
                if (lesson != null || !first) {
                    if (nowTime.isBefore(item.caption.endTime.minusMinutes(10))) {
                        if (first && nowTime.isBefore(item.caption.beginTime.minusHours(3))) {
                            updateTime = day.day.date.toLocalDateTime(item.caption.beginTime.minusHours(3));
                        } else if (first && nowTime.isBefore(item.caption.beginTime.minusHours(1))) {
                            updateTime = day.day.date.toLocalDateTime(item.caption.beginTime.minusHours(1));
                        } else {
                            updateTime = day.day.date.toLocalDateTime(item.caption.endTime.minusMinutes(10));
                        }
                        break
                    }
                    first = false
                }
            }
            index++;
        }

        return updateTime
    }
}
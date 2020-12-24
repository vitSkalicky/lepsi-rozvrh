package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import cz.vitskalicky.lepsirozvrh.items.OldRozvrh.GetNLreturnValues
import cz.vitskalicky.lepsirozvrh.items.OldRozvrhHodina
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson
import org.joda.time.LocalDate
import org.joda.time.LocalTime

data class DayRelated(
        @Embedded val day: RozvrhDay,

        @Relation(
                parentColumn = "date",
                entityColumn = "day",
                entity = RozvrhBlock::class
        )
        /**
         * Rozvrh Blocks in the day ordered by caption
         */
        val blocks: List<BlockRelated>
){
    /**
     * returns the lesson block, which should be highlighted to the user as next or current lesson, or null
     * if the school is over or this is not the current day.
     *
     * @param forNotification If true, the first lesson won't be highlighted up until one hour before its start
     */
    fun getHighlightBlock(forNotification: Boolean): BlockRelated? {
        val nowDate = LocalDate.now()
        val nowTime = LocalTime.now()

        if (nowDate != day.date){
            return null
        }

        var first = true
        var lessonIndex = 0

        for (i in blocks.indices) {
            val item: BlockRelated = blocks[i]
            val lesson: RozvrhLesson? = item.block.lessons.getOrNull(0)
            if (lesson != null || !first) {
                if (forNotification && first && nowTime.isBefore(item.caption.beginTime.minusHours(1))) { //do not highlight
                    return null
                }
                if (nowTime.isBefore(item.caption.endTime.minusMinutes(10))) {
                    return item
                }
                first = false
            }
        }

        return null
    }

    /**
     * Returns lessons that should be displayed on a widget or an empty list if all the lessons are already over. If there is en event on the day ([RozvrhDay.event] != null),
     * the list is `null` and the string contains name of the event. Otherwise the string is `null`.
     *
     * If this is not today, the pair is `null`.
     *
     * @param length how many lessons does the widget display - determines the length of the returned list.
     * @return a [Pair] of nullable list and nullable string or `null` if this is not today.
     * The first parameter is list of lessons which should be displayed or empty list if all the lessons
     * are already over or `null` if there is an event on that day. The second parameter is the description of current event or `null`
     * if there is no event on that day.
     */
    fun getWidgetDisplayBlocks(length: Int): Pair<List<BlockRelated>?,String?>?{
        val nowDate = LocalDate.now()
        val nowTime = LocalTime.now()
        if (nowDate != day.date){
            return null
        }
        if (day.event != null){
            return Pair(null, day.event)
        }
        var nowIndex = 0;
        while (blocks[nowIndex].caption.endTime.minusMinutes(10).isBefore(nowTime) && nowIndex < blocks.size){
            nowIndex++
        }
        val ret = ArrayList<BlockRelated>()
        for (i in 0 until length){
            if (nowIndex + i < blocks.size){
                ret.add(blocks[nowIndex + i])
            }
        }
        return Pair(ret, null)
    }
}
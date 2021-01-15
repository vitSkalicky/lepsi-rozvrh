package cz.vitskalicky.lepsirozvrh.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.HorizontalScrollView
import android.widget.Toast
import cz.vitskalicky.lepsirozvrh.R
import cz.vitskalicky.lepsirozvrh.SharedPrefs
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.RozvrhAPI
import cz.vitskalicky.lepsirozvrh.model.relations.BlockRelated
import cz.vitskalicky.lepsirozvrh.model.relations.DayRelated
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhBlock
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson
import io.sentry.Sentry
import io.sentry.event.BreadcrumbBuilder
import org.joda.time.LocalDate

class RozvrhLayout : ViewGroup {
    /**
     * Creates a cell with reasonably long data and calculates its minimum width
     */
    private var naturalCellWidth = -1
        private get() {
            if (field != -1 && childHeightWhenCalculatingNaturalCellWidth == childHeight) {
                return field
            }
            val view = HodinaView(context, null)
            val minWidth = Math.max(view.measureExampleWidth(), CellView.goldenRectangle(childHeight))
            field = minWidth
            childHeightWhenCalculatingNaturalCellWidth = childHeight
            return minWidth
        }
    private var childHeightWhenCalculatingNaturalCellWidth = -1
    private var rozvrh: RozvrhRelated? = null
    private var perm = false
    private var rows = 0 //only actual lessons - add 1 to calculate with captions as well
    private var columns = 0 //only actual lessons - add 1 to calculate with day cells as well
    private var childHeight = 0
    private var cornerView: CornerView? = null
    private var denViews = ArrayList<DenView>(0)
    private var captionViews = ArrayList<CaptionView>(0)
    private var nextHodinaView: HodinaView? = null //the highlighted one
    private var nextHodinaViewRight: HodinaView? = null //the right one from the highlighted one (it has its left highlighted)
    private var nextHodinaViewBottom: HodinaView? = null //the bottom one from the highlighted one (it has its top highlighted)
    private var nextHodinaViewCorner: HodinaView? = null //the corner one from the highlighted one (it has its corner highlighted)
    private var hodinasByCaptions: Array<Array<ArrayList<HodinaView>>> = Array(0) { Array(0){ ArrayList() } } //the first paramemter is caption index, second day and the list contains all lessons in that block
    private var hodinaViewRecycler: HodinaViewRecycler = HodinaViewRecycler(context)
    private var columnSizes = IntArray(1) // includes days column

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWS = MeasureSpec.getSize(widthMeasureSpec)
        val specWM = MeasureSpec.getMode(widthMeasureSpec)
        val specHS = MeasureSpec.getSize(heightMeasureSpec)
        val specHM = MeasureSpec.getMode(heightMeasureSpec)
        var width = specWS
        val height = specHS
        var childState = 0
        childHeight = Math.ceil(specHS.toDouble() / (rows + 1)).toInt()
        val naturalCellWidth = naturalCellWidth

        //calculate width of every column
        columnSizes[0] = naturalCellWidth
        for (i in denViews.indices) {
            columnSizes[0] = Math.max(columnSizes[0], denViews[i].minimumWidth)
        }
        for (i in 1 until columnSizes.size) {
            columnSizes[i] = Math.max(naturalCellWidth, captionViews[i - 1].minimumWidth)
            for (j in 0 until rows) {
                var max = 0
                var count = 0
                for (item in hodinasByCaptions[i - 1][j]) {
                    max = Math.max(max, item.minimumWidth)
                    count++
                }
                columnSizes[i] = Math.max(columnSizes[i], max * count)
            }
        }
        var prefferedWidth = 0
        for (columnSize in columnSizes) {
            prefferedWidth += columnSize
        }
        if (specWM == MeasureSpec.UNSPECIFIED || specWM == MeasureSpec.AT_MOST && prefferedWidth <= specWS) {
            width = prefferedWidth
        } else {
            val widthRatio = specWS / prefferedWidth.toFloat()
            for (i in columnSizes.indices) {
                columnSizes[i] = Math.floor((columnSizes[i] * widthRatio).toDouble()).toInt()
            }
            width = specWS
        }
        if (rows == 0 || columns == 0) {
            setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                    resolveSizeAndState(specHS, heightMeasureSpec,
                            childState shl MEASURED_HEIGHT_STATE_SHIFT))
            return
        }
        val childHeightMS = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
        cornerView?.let{
            measureChild(it, MeasureSpec.makeMeasureSpec(columnSizes[0], MeasureSpec.EXACTLY), childHeightMS)
            childState = combineMeasuredStates(childState, it.measuredState)
        }
        for (item in denViews) {
            measureChild(item, MeasureSpec.makeMeasureSpec(columnSizes[0], MeasureSpec.EXACTLY), childHeightMS)
            childState = combineMeasuredStates(childState, item.measuredState)
        }
        for (i in captionViews.indices) {
            val item = captionViews[i]
            val hodinaWidthMS = MeasureSpec.makeMeasureSpec(columnSizes[i + 1], MeasureSpec.EXACTLY)
            measureChild(item, hodinaWidthMS, childHeightMS)
            childState = combineMeasuredStates(childState, item.measuredState)
        }
        for (i in hodinasByCaptions.indices) {
            for (j in 0 until hodinasByCaptions[i].size) {
                for (item in hodinasByCaptions[i][j]) {
                    val hodinaWidthMS = MeasureSpec.makeMeasureSpec((columnSizes[i] / hodinasByCaptions[i][j].size), MeasureSpec.EXACTLY)
                    measureChild(item, hodinaWidthMS, childHeightMS)
                    childState = combineMeasuredStates(childState, item.measuredState)
                }
            }
        }
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(specHS, heightMeasureSpec,
                        childState shl MEASURED_HEIGHT_STATE_SHIFT))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (rows == 0 || columns == 0) {
            return
        }
        cornerView?.layout(l, t, l + columnSizes[0], t + childHeight)
        for (i in denViews.indices) {
            denViews[i].layout(l, t + (i + 1) * childHeight, l + columnSizes[0], t + (i + 2) * childHeight)
        }
        var prevColumnEnd = l + columnSizes[0]
        for (i in captionViews.indices) {
            val thisColumnEnd = prevColumnEnd + columnSizes[i + 1]
            if (i == columns - 1) {
                //the last one
                captionViews[i].layout(prevColumnEnd, t, r, t + childHeight)
            } else {
                captionViews[i].layout(prevColumnEnd, t, thisColumnEnd, t + childHeight)
            }
            prevColumnEnd = thisColumnEnd
        }
        prevColumnEnd = l + columnSizes[0]
        for (i in 0 until columns) {
            val thisColumnEnd = prevColumnEnd + columnSizes[i + 1]
            for (j in 0 until rows) {
                val cellWidth: Int = if (hodinasByCaptions[i][j].size > 0) {
                    columnSizes[i + 1] / hodinasByCaptions[i][j].size
                } else {
                    columnSizes[i + 1]
                }
                var lastCellEnd = prevColumnEnd
                val views: List<HodinaView> = hodinasByCaptions[i][j]
                for (k in views.indices) {
                    val item = views[k]
                    if (i == columns - 1 && k == views.size - 1) {
                        //the last one
                        item.layout(lastCellEnd, t + childHeight + j * childHeight, r, t + childHeight + (j + 1) * childHeight)
                    } else {
                        item.layout(lastCellEnd, t + childHeight + j * childHeight, lastCellEnd + cellWidth, t + childHeight + (j + 1) * childHeight)
                    }
                    lastCellEnd += cellWidth
                }
            }
            prevColumnEnd = thisColumnEnd
        }
    }

    fun createViews() {
        //debug timing: Log.d(TAG_TIMER, "createViews start " + Utils.getDebugTime());
        if (rows == 0 && columns == 0) {
            rows = RozvrhAPI.getRememberedRows(context)
            columns = RozvrhAPI.getRememberedColumns(context)
        }
        for (i in hodinasByCaptions.indices) {
            for (j in hodinasByCaptions[i].indices) {
                for (item in hodinasByCaptions[i][j]) {
                    removeView(item)
                    hodinaViewRecycler.store(item)
                }
                hodinasByCaptions[i][j].clear()
            }
        }
        if (denViews.size == rows && captionViews.size == columns && hodinasByCaptions.size == columns && (hodinasByCaptions.size == 0 || hodinasByCaptions[0].size == rows) && cornerView != null) {
            //debug timing: Log.d(TAG_TIMER, "createViews end " + Utils.getDebugTime());
            return
        }
        removeAllViews()
        denViews = ArrayList()
        captionViews = ArrayList()
        hodinasByCaptions = Array<Array<ArrayList<HodinaView>>>(columns) { Array(rows){ ArrayList() } }

        if (cornerView == null) {
            cornerView = CornerView(context, null)
        }
        addView(cornerView)
        for (i in 0 until columns) {
            val item = CaptionView(context, null)
            captionViews.add(item)
            addView(item)
        }
        for (i in 0 until rows) {
            val denCell = DenView(context, null)
            denViews.add(denCell)
            addView(denCell)
        }

        //debug timing: Log.d(TAG_TIMER, "createViews end " + Utils.getDebugTime());
    }

    fun setRozvrh(rozvrh: RozvrhRelated?, centerToCurrentlesson: Boolean) {
        //debug timing: Log.d(TAG_TIMER, "populate start " + Utils.getDebugTime());
        //todo sentry extra
        /*if (rozvrh != null) {
            Sentry.getContext().addExtra("rozvrh", oldRozvrh.getStructure())
            Log.d(TAG, """
     Rozvrh structure:
     ${oldRozvrh.getStructure()}
     """.trimIndent())
        } else {
            Sentry.getContext().addExtra("rozvrh", "null")
            Log.d(TAG, """
     Rozvrh structure:
     null
     """.trimIndent())
        }*/
        this.rozvrh = rozvrh
        if (rozvrh == null) {
            empty()
            return
        }
        rows = rozvrh.days.size
        columns = rozvrh.captions.size
        perm = rozvrh.rozvrh.permanent
        columnSizes = IntArray(columns + 1)
        createViews()
        RozvrhAPI.rememberRows(context, rows)
        RozvrhAPI.rememberColumns(context, columns)

        //populate
        cornerView?.text = rozvrh.rozvrh.cycle?.name ?: ""
        for (i in 0 until columns) {
            captionViews[i].caption = rozvrh.captions[i]
        }
        for (i in 0 until rows) {
            val den: DayRelated = rozvrh.days[i]
            denViews[i].rozvrhDay = den.day

            den.blocks.forEach {
                val blck = it
                it.block.lessons.forEach {
                    val view = hodinaViewRecycler.retrieve()
                    view.setHodina(it, perm)
                    addView(view)
                    hodinasByCaptions[blck.caption.index][i].add(view)
                }
                it.block.lessons.ifEmpty {
                    val view = hodinaViewRecycler.retrieve()
                    view.setHodina(null, perm)
                    addView(view)
                    hodinasByCaptions[blck.caption.index][i].add(view)
                }
            }
        }
        highlightCurrentLesson()
        if (centerToCurrentlesson) centerToCurrentLesson()
        invalidate()
        requestLayout()

        //debug timing: Log.d(TAG_TIMER, "populate end " + Utils.getDebugTime());
    }

    var displayingWtfRozvrhDialog = false
    fun highlightCurrentLesson() {
        if (rozvrh == null) {
            nextHodinaView = null
            nextHodinaViewRight = null
            nextHodinaViewBottom = null
            nextHodinaViewCorner = null
            return
        }
        val toHighlight: BlockRelated? = rozvrh?.getHighlightBlock(false)


        //unhighlight
        nextHodinaView?.hightlightEdges(false, false, false)
        nextHodinaView?.highlightEntire(false)
        nextHodinaViewRight?.hightlightEdges(false, false, false)
        nextHodinaViewBottom?.hightlightEdges(false, false, false)
        nextHodinaViewCorner?.hightlightEdges(false, false, false)

        nextHodinaView = null
        nextHodinaViewRight = null
        nextHodinaViewBottom = null
        nextHodinaViewCorner = null
        if (toHighlight == null) {
            return
        }
        val block: RozvrhBlock = toHighlight.block
        val day:LocalDate = block.day
        val caption: RozvrhCaption = toHighlight.caption
        val dayIndex: Int = rozvrh?.days?.indexOfFirst { it.day.date == day }.takeUnless { it==-1 } ?: return

        //fail-safe
        /*if (hodinaIndex >= hodinasByCaptions.size || denIndex >= hodinasByCaptions[hodinaIndex].length) {
            // I've never seen such rozvrh
            Log.w(TAG, "There are more lessons than captions in a weekly schedule. Showing WTF rozvrh dialog.")
            Sentry.getContext().recordBreadcrumb(BreadcrumbBuilder().setMessage("There are more lessons than captions in a weekly schedule. Showing WTF rozvrh dialog.").build())
            if (!displayingWtfRozvrhDialog) {
                try {
                    Utils.wtfRozvrh(context, this, oldRozvrh.getDny().get(0).getParsedDatum())
                    displayingWtfRozvrhDialog = true
                } catch (e: Exception) {
                    Toast.makeText(context, "!", Toast.LENGTH_SHORT).show()
                }
            }
            hodinaIndex = Math.min(hodinaIndex, hodinasByCaptions.size - 1)
            denIndex = Math.min(denIndex, hodinasByCaptions[hodinaIndex].length - 1)
        }*/

        nextHodinaView = hodinasByCaptions[caption.index][dayIndex].firstOrNull() ?: return //todo report error. this should not happen

        nextHodinaView?.hightlightEdges(true, true, true)
        nextHodinaView?.highlightEntire(true)
        if (dayIndex + 1 < rows && caption.index < columns) {
            nextHodinaViewBottom = hodinasByCaptions[caption.index][dayIndex + 1].firstOrNull()
            nextHodinaViewBottom?.hightlightEdges(true, false, true)
        }
        if (dayIndex < rows && caption.index + 1 < columns) {
            nextHodinaViewRight = hodinasByCaptions[caption.index + 1][dayIndex].firstOrNull()
            nextHodinaViewRight?.hightlightEdges(false, true, true)
        }
        if (dayIndex + 1 < rows && caption.index + 1 < columns) {
            nextHodinaViewCorner = hodinasByCaptions[caption.index + 1][dayIndex + 1].firstOrNull()
            nextHodinaViewCorner?.hightlightEdges(false, false, true)
        }
    }

    // we want to center when: the user opens the app, user taps current week
    // we don't want to center when: a fresh schedule with minor changes loads, user switches to the schedule using arrows.
    fun centerToCurrentLesson() {
        if (!SharedPrefs.getBooleanPreference(context, R.string.PREFS_CENTER_TO_CURRENT_LESSON, true)) return
        val parent = parent
        if (parent is HorizontalScrollView) {
            val hsvParent = parent
            val viewTreeObserver = hsvParent.viewTreeObserver
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (nextHodinaView != null) {
                        val parentWidth = hsvParent.width
                        hsvParent.smoothScrollTo(nextHodinaView!!.x.toInt() - parentWidth / 2 + nextHodinaView!!.width / 2, 0)
                    }
                }
            })
        }
    }

    /**
     * Empty the table when loading to prevent confusion
     */
    fun empty() {
        rozvrh = null

        //clear
        cornerView?.text = ""
        captionViews.forEach { it.caption = null }
        denViews.forEach { it.rozvrhDay = null }
        for (i in 0 until columns) {
            for (j in 0 until rows) {
                if (hodinasByCaptions[i][j].size == 0) {
                    val view = hodinaViewRecycler.retrieve()
                    addView(view)
                    hodinasByCaptions[i][j].add(view)
                }
                hodinasByCaptions[i][j].forEach { it.setHodina(null, perm) }
            }
        }
        invalidate()
        requestLayout()
        //debug timing: Log.d(TAG_TIMER, "populate end " + Utils.getDebugTime());
    }

    companion object {
        val TAG = RozvrhLayout::class.java.simpleName
    }
}
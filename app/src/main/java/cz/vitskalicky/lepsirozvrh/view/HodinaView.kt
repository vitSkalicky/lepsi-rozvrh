package cz.vitskalicky.lepsirozvrh.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.R
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.items.OldRozvrhHodina
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson
import cz.vitskalicky.lepsirozvrh.theme.Theme
import kotlin.math.max

class HodinaView(context: Context?, attrs: AttributeSet?) : CellView(context, attrs) {
    private var hodina: RozvrhLesson? = null
    private var perm = false
    private val mistPaint: Paint
    private val highlightPaint: Paint
    private val highlightedDividerPaint: Paint
    private val homeworkPaint: Paint
    private val highlightWidth: Int
    private val homeworkSize: Int
    private var topHighlighted = false
    private var leftHighlighted = false
    private var cornerHighlighted = false
    private var entireHighlighted //the highlighting is thicker
            = false

    override fun getMinimumWidth(): Int {
        return if (hodina != null) {
            val hodinan: RozvrhLesson = hodina!!
            var zkrpr = hodinan.subjectAbbrev
            if (zkrpr.isEmpty()) zkrpr = hodinan.subjectName

            var zkrmist = hodinan.roomAbbrev

            var zkruc: String? = hodinan.teacherAbbrev

            if ((context.applicationContext as MainApplication).login.isTeacher()) {
                // to teacher's we want to show the class, not the teacher
                // the class name is saved in zkrskup and skup
                zkruc = hodinan.groups.joinToString(", ") { if (it.abbrev.isBlank()) {it.abbrev} else {it.name} }
            }
            val padding = super.getMinimumWidth()
            val primaryText = primaryTextPaint.measureText(zkrpr).toInt() + 1
            val secondaryText = (secondaryTextPaint.measureText("$zkruc ") + mistPaint.measureText(zkrmist)).toInt() + 1
            padding + max(primaryText, secondaryText)
        } else {
            super.getMinimumWidth()
        }
    }

    /**
     * Measures what the minimal width would be for an example cell with reasonably long texts.
     */
    fun measureExampleWidth(): Int {
        val padding = super.getMinimumWidth()
        val primaryText = primaryTextPaint.measureText("MATH").toInt() + 1
        val secondaryText = (secondaryTextPaint.measureText("Tchr" + " ") + mistPaint.measureText("VIII.B")).toInt() + 1
        return padding + Math.max(primaryText, secondaryText)
    }

    /**
     * When the texts are packed tightly together
     */
    override fun getMinimumHeight(): Int {
        return super.getMinimumHeight() + primaryTextSize + textPadding + secondaryTextSize
    }

    /**
     * When the subject text is aligned to the center
     */
    val minimalComfortableHeight: Int
        get() = (primaryTextSize / 2 + textPadding + secondaryTextSize) * 2 + super.getMinimumHeight()

    /**
     * Updates the content
     */
    fun setHodina(hodina: RozvrhLesson?, perm: Boolean) {
        this.hodina = hodina
        this.perm = perm
        if (hodina == null) {
            backgroundPaint.color = t.cEmptyBg
            primaryTextPaint.color = t.chPrimaryText
            secondaryTextPaint.color = t.chSecondaryText
            mistPaint.color = t.chRoomText
        } else if (hodina.changeType == RozvrhLesson.CHANGED) {
            backgroundPaint.color = t.cChngBg
            primaryTextPaint.color = t.cChngPrimaryText
            secondaryTextPaint.color = t.cChngSecondaryText
            mistPaint.color = t.cChngRoomText
        } else if (hodina.changeType == RozvrhLesson.CANCELLED) {
            backgroundPaint.color = t.caBg
            primaryTextPaint.color = t.caPrimaryText
            secondaryTextPaint.color = t.caSecondaryText
            mistPaint.color = t.caRoomText
        } else if (hodina.changeType == RozvrhLesson.NO_CHANGE) {
            backgroundPaint.color = t.chBg
            primaryTextPaint.color = t.chPrimaryText
            secondaryTextPaint.color = t.chSecondaryText
            mistPaint.color = t.chRoomText
        }
        invalidate()
        requestLayout()
    }

    fun getHodina(): RozvrhLesson? {
        return hodina
    }

    fun hightlightEdges(top: Boolean, left: Boolean, corner: Boolean) {
        topHighlighted = top
        leftHighlighted = left
        cornerHighlighted = corner
    }

    fun highlightEntire(highlight: Boolean) {
        entireHighlighted = highlight
        hightlightEdges(highlight, highlight, highlight)
    }

    override fun onDraw(canvas: Canvas) {
        setDrawDividers(!topHighlighted, !cornerHighlighted, !leftHighlighted)
        super.onDraw(canvas)
        val w = width
        val h = height

        //# draw highlighted dividers
        //left
        if (leftHighlighted || entireHighlighted) {
            canvas.drawLine(dividerWidth.toFloat() / 2, dividerWidth.toFloat(), dividerWidth.toFloat() / 2, h.toFloat(), highlightedDividerPaint)
        }

        //top
        if (topHighlighted || entireHighlighted) {
            canvas.drawLine(dividerWidth.toFloat(), dividerWidth.toFloat() / 2, w.toFloat(), dividerWidth.toFloat() / 2, highlightedDividerPaint)
        }

        //corner
        if (cornerHighlighted || entireHighlighted) {
            canvas.drawPoint(dividerWidth / 2f, dividerWidth / 2f, highlightedDividerPaint)
        }

        //highlight
        if (entireHighlighted) {
            canvas.drawLine(dividerWidth.toFloat(), dividerWidth + highlightWidth / 2f, w.toFloat(), dividerWidth + highlightWidth / 2f, highlightPaint)
            canvas.drawLine(w - highlightWidth / 2f, dividerWidth + highlightWidth / 2f, w - highlightWidth / 2f, h - highlightWidth / 2f, highlightPaint)
            canvas.drawLine(w.toFloat(), h - highlightWidth / 2f, dividerWidth.toFloat(), h - highlightWidth / 2f, highlightPaint)
            canvas.drawLine(dividerWidth + highlightWidth / 2f, h - highlightWidth / 2f, dividerWidth + highlightWidth / 2f, dividerWidth + highlightWidth / 2f, highlightPaint)
        }
    }

    override fun onDrawContent(canvas: Canvas, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) {
        val h = yEnd - yStart
        val w = xEnd - xStart

        //# draw texts
        if (hodina != null) {
            val lesson: RozvrhLesson = hodina!!
            val zkrpr: String = lesson.subjectAbbrev.let { if (it.isBlank()){lesson.subjectName}else{it} }

            val zkrmist: String = lesson.roomAbbrev

            var zkruc: String = lesson.teacherAbbrev

            if ((context.applicationContext as MainApplication).login.isTeacher()) {
                // to teacher's we want to show the class, not the teacher
                // the class name is saved in zkrskup and skup
                zkruc = lesson.groups.joinToString(", ") { if (it.abbrev.isBlank()) {it.abbrev} else {it.name} }
            }

            var actualSecondaryTextSize: Float = if ((zkrmist + zkruc).isEmpty()) 0.0f else secondaryTextSize.toFloat()
            var actualPrimaryTextSize = primaryTextSize.toFloat()
            if (canvas.height < minimumHeight) {
                var overflow = actualPrimaryTextSize + textPadding + actualSecondaryTextSize - h
                if (overflow < 0) {
                    overflow = 0f
                }
                actualPrimaryTextSize = actualPrimaryTextSize - overflow / ((actualPrimaryTextSize + actualSecondaryTextSize) / actualPrimaryTextSize)
                if (actualSecondaryTextSize > 0) {
                    actualSecondaryTextSize = actualSecondaryTextSize - overflow / ((primaryTextSize + actualSecondaryTextSize) / actualSecondaryTextSize)
                }
            }
            primaryTextPaint.textSize = actualPrimaryTextSize
            secondaryTextPaint.textSize = actualSecondaryTextSize
            mistPaint.textSize = actualSecondaryTextSize
            var zkrprBaseline = h / 2f + actualPrimaryTextSize / 2f
            val middle = w / 2f
            var secondaryBaseline = zkrprBaseline + textPadding + actualSecondaryTextSize
            val secondaryTextWidth = secondaryTextPaint.measureText("$zkruc $zkrmist")
            val zkrucStart = middle - secondaryTextWidth / 2f
            val zkrmistStart = zkrucStart + secondaryTextPaint.measureText("$zkruc ")
            if (canvas.height < minimalComfortableHeight - (secondaryTextSize - actualSecondaryTextSize)) {
                //do not align zkrpr to center (vertically)
                //secondary text will be aligned to the bottom and zkrpr to the center of the remaining space
                secondaryBaseline = h.toFloat()
                zkrprBaseline = (secondaryBaseline - actualSecondaryTextSize) / 2 + actualPrimaryTextSize / 2f
            }

            // zkrpr
            primaryTextPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(zkrpr, middle + xStart, zkrprBaseline + yStart, primaryTextPaint)

            //draw secondary = teacher and room
            mistPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(zkrmist, zkrmistStart + xStart, secondaryBaseline + yStart, mistPaint)
            secondaryTextPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(zkruc, zkrucStart + xStart, secondaryBaseline + yStart, secondaryTextPaint)

            //draw little dot if there is a homework
            if (lesson.homeworkIds.isNotEmpty()) {
                var use: Paint? = homeworkPaint
                if (!Theme.Utils.isLegible(homeworkPaint.color, backgroundPaint.color, 1.5)) {
                    use = primaryTextPaint
                }
                canvas.drawCircle((xEnd - homeworkSize).toFloat(), (yStart + homeworkSize).toFloat(), homeworkSize.toFloat(), use!!)
            }

            /*// draw cycle
            if (perm && hodina.getCycle() != null && !hodina.getCycle().isEmpty()){
                float cycleBaseline = zkrprBaseline - primaryTextSize - textPadding;
                secondaryTextPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(hodina.getCycle(), middle, cycleBaseline, secondaryTextPaint);
            }*/
        }
    }

    private fun addField(layout: TableLayout, resId: Int, fieldText: String?): Boolean {
        return if (fieldText != null && !fieldText.isEmpty()) {
            val tr = LayoutInflater.from(context).inflate(R.layout.lesson_details_dialog_row, null) as TableRow
            val tw1 = tr.findViewById<TextView>(R.id.textViewKey)
            val tw2 = tr.findViewById<TextView>(R.id.textViewValue)
            tw1.text = context.getString(resId)
            tw2.text = fieldText
            //tw2.setMaxLines(8000);
            //tr.addView(tw1);
            //tr.addView(tw2,new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.addView(tr, TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            true
        } else {
            false
        }
    }

    fun showDetailDialog() {
        if (hodina == null) return
        val lesson: RozvrhLesson = hodina!!
        val builder = AlertDialog.Builder(context)
        builder.setTitle(lesson.subjectName.ifBlank { lesson.subjectAbbrev })

        val tableLayout = TableLayout(context)
        tableLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val density = context.resources.displayMetrics.density.toInt()
        tableLayout.setPadding(24 * density, 16 * density, 24 * density, 0)
        addField(tableLayout, R.string.homework, lesson.homeworkIds.size.toString())
        if (perm) {
            addField(tableLayout, R.string.cycle, lesson.cycles.joinToString(", "){ it.abbrev.ifBlank { it.name }})
        }
        addField(tableLayout, R.string.group, lesson.groups.joinToString(", "){ it.abbrev.ifBlank { it.name }}) //you don't see group on the simplified tile anymore, therefore it is one of the main reasons you may want to see this dialog
        addField(tableLayout, R.string.lesson_teacher, lesson.teacherName.ifBlank { lesson.teacherAbbrev })
        addField(tableLayout, R.string.room, lesson.roomName.ifBlank { lesson.roomAbbrev })
        addField(tableLayout, R.string.subject_name, lesson.subjectName.ifBlank { lesson.subjectAbbrev })
        addField(tableLayout, R.string.topic, lesson.theme)
        addField(tableLayout, R.string.change, lesson.changeDescription)
        builder.setView(tableLayout)
        builder.setPositiveButton(R.string.close) { dialog, which -> }
        val dialog = builder.create()
        dialog.show()
    }

    init {
        mistPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mistPaint.color = t.chRoomText
        mistPaint.textSize = secondaryTextSize.toFloat()
        mistPaint.typeface = Typeface.DEFAULT
        mistPaint.textAlign = Paint.Align.LEFT
        highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        highlightPaint.color = t.cHighlight
        highlightWidth = t.pxHighlightWidth
        highlightPaint.strokeWidth = highlightWidth.toFloat()
        highlightedDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        highlightedDividerPaint.color = t.cHighlight
        highlightedDividerPaint.strokeWidth = dividerWidth.toFloat()
        homeworkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        homeworkPaint.color = t.cHomework
        homeworkSize = t.pxHomework
        setOnClickListener { v: View? -> showDetailDialog() }
        setDrawDividers(true, true, true)
    }
}
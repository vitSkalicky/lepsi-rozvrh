package cz.vitskalicky.lepsirozvrh.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import com.jaredrummler.cyanea.Cyanea
import cz.vitskalicky.lepsirozvrh.*
import cz.vitskalicky.lepsirozvrh.activity.MainActivity
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatus
import cz.vitskalicky.lepsirozvrh.settings.SettingsActivity
import cz.vitskalicky.lepsirozvrh.theme.Theme
import cz.vitskalicky.lepsirozvrh.view.RozvrhLayout
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatus.Status.*
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat

class RozvrhFragment : Fragment() {

    private val viewModel: RozvrhViewModel by viewModels()

    private lateinit var rozvrhLayout: RozvrhLayout

    private lateinit var  bottomAppBar: Toolbar
    //private lateinit var  rtFragment: RozvrhTableFragment

    private lateinit var  ibSettings: ImageButton
    private lateinit var  ibPrev: ImageButton
    private lateinit var  ibCurrent: ImageButton
    private lateinit var  ibPermanent: ImageButton
    private lateinit var  ibNext: ImageButton
    private lateinit var  ibRefresh: ImageButton
    private lateinit var  progressBar: ProgressBar

    /**
     * if set to true, the scroll view will jump to to the current lesson next time a valid schedule is loaded. Then this variable is reset to false.
     */
    var centerToCurrentLesson = false
    public set(value) {
        field = value
    }

    private lateinit var  infoLine: TextView
    //private lateinit var  displayInfo: DisplayInfo

    var showedNotiInfo = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.rozvrh_fragment, container, false)
        rozvrhLayout = rootView.findViewById(R.id.rozvrhLayout)

        bottomAppBar = rootView.findViewById<Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(bottomAppBar)

        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayShowCustomEnabled(false)
        }else{
            Log.e(this::class.qualifiedName, "Activity is not AppCompatActivity and thus actionbar could not be set up!")
        }

        //displayInfo = DisplayInfo()
        infoLine = rootView.findViewById<TextView>(R.id.infoLine)
        /*displayInfo.addOnMessageChangeListener { oldMessage: String?, newMessage: String? ->
            infoLine.text = newMessage
            if (displayInfo.errorMessage != null) {
                TooltipCompat.setTooltipText(ibRefresh, displayInfo.errorMessage)
            } else {
                TooltipCompat.setTooltipText(ibRefresh, getText(R.string.refresh))
            }
        }*/

        ibSettings = rootView.findViewById<ImageButton>(R.id.settings)
        ibPrev = rootView.findViewById<ImageButton>(R.id.prev)
        ibCurrent = rootView.findViewById<ImageButton>(R.id.curent)
        ibPermanent = rootView.findViewById<ImageButton>(R.id.permanent)
        ibNext = rootView.findViewById<ImageButton>(R.id.next)
        ibRefresh = rootView.findViewById<ImageButton>(R.id.refresh)
        progressBar = rootView.findViewById<ProgressBar>(R.id.progressBar)

        //todo display status
        progressBar.visibility = View.GONE

        rozvrhLayout.createViews()

        ibSettings.setOnClickListener { view: View? ->
            if (activity != null) {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
            }else{
                Log.e(this::class.qualifiedName, "Could not launch setting activity, as gatActivity() returned null!")
            }
        }
        TooltipCompat.setTooltipText(ibSettings, getText(R.string.settings))
        TooltipCompat.setTooltipText(ibPrev, getText(R.string.prev_week))
        TooltipCompat.setTooltipText(ibCurrent, getText(R.string.current_week))
        TooltipCompat.setTooltipText(ibPermanent, getText(R.string.permanent_schedule))
        TooltipCompat.setTooltipText(ibNext, getText(R.string.next_week))
        TooltipCompat.setTooltipText(ibRefresh, getText(R.string.refresh))
        ibPrev.setOnClickListener { v: View? ->
            viewModel.weekPosition--
            showHideButtons()
        }
        ibNext.setOnClickListener { v: View? ->
            viewModel.weekPosition++
            showHideButtons()
        }
        ibCurrent.setOnClickListener { v: View? ->
            centerToCurrentLesson = true
            viewModel.weekPosition = 0
            showHideButtons()
        }
        ibPermanent.setOnClickListener { v: View? ->
            viewModel.weekPosition = RozvrhViewModel.PERM
            showHideButtons()
        }
        ibRefresh.setOnClickListener { v: View? -> viewModel.forceRefresh() }

        ibSettings.setOnLongClickListener { v: View? ->
            if (BuildConfig.DEBUG) {
                SharedPrefs.setString(context, SharedPrefs.SEMESTER_END, ISODateTimeFormat.dateTime().print(DateTime.now().minusWeeks(1)))
                SharedPrefs.setString(context, SharedPrefs.ACCEESS_TOKEN, "afddfgdf")
                SharedPrefs.setString(context, SharedPrefs.REFRESH_TOKEN, "afddfgdf")
                DebugUtils.getInstance(context).isDemoMode = !DebugUtils.getInstance(context).isDemoMode
                Toast.makeText(context, "Demo mode changed", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            } else {
                return@setOnLongClickListener false
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getDisplayLD().distinctUntilChanged().observe(viewLifecycleOwner){
            Log.d("Snek","data received at ${LocalDateTime.now()}")
            rozvrhLayout.setRozvrh(it, centerToCurrentLesson)
            //reset center to current lesson once it happened
            if (it != null){
                centerToCurrentLesson = false
            }
            updateInfoLine()
        }
        viewModel.getStatusLD().observe(viewLifecycleOwner){
            when(it.status){
                SUCCESS, UNKNOWN -> {
                    progressBar.visibility = View.GONE
                    ibRefresh.visibility = View.VISIBLE
                    ibRefresh.setImageDrawable(context?.getDrawable(R.drawable.ic_refresh_black_24))
                    TooltipCompat.setTooltipText(ibRefresh, getText(R.string.refresh))
                }
                ERROR -> {
                    progressBar.visibility = View.GONE
                    ibRefresh.visibility = View.VISIBLE
                    ibRefresh.setImageDrawable(context?.getDrawable(R.drawable.ic_refresh_problem_black_24dp))
                    TooltipCompat.setTooltipText(ibRefresh, getText(it.errMessage ?: R.string.refresh))

                    activity?.let {
                        if (it is MainActivity){
                            //makes sure it does not launch login activity twice
                            it.checkLogin()
                        }else{
                            //this is technically redundant. just future proof.
                            if ((context?.applicationContext as? MainApplication)?.login?.checkLogin(it) != null){
                                activity?.finish()
                            }
                        }
                    }

                }
                LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    ibRefresh.visibility = View.GONE
                }
            }
            updateInfoLine()
        }
        viewModel.isOfflineLD.observe(viewLifecycleOwner) {
            updateInfoLine()
        }
        Log.d("Snek","View created at ${LocalDateTime.now()}")
    }

    override fun onResume() {
        super.onResume()
        if (!SharedPrefs.containsPreference(context, R.string.PREFS_SHOW_INFO_LINE) || SharedPrefs.getBooleanPreference(context, R.string.PREFS_SHOW_INFO_LINE)) {
            infoLine.visibility = View.VISIBLE
        } else {
            infoLine.visibility = View.GONE
        }

        if (centerToCurrentLesson && viewModel.getDisplayLD().value != null){
            rozvrhLayout.centerToCurrentLesson()
            centerToCurrentLesson = false
        }
        rozvrhLayout.highlightCurrentLesson()


        val iconColor = Cyanea.instance.menuIconColor
        ibSettings.setColorFilter(iconColor)
        ibPrev.setColorFilter(iconColor)
        ibCurrent.setColorFilter(iconColor)
        ibPermanent.setColorFilter(iconColor)
        ibNext.setColorFilter(iconColor)
        ibRefresh.setColorFilter(iconColor)

        val t = Theme(context)
        infoLine.setBackgroundColor(t.cInfolineBg)
        infoLine.textSize = t.spInfolineTextSize
        infoLine.setTextColor(t.cInfolineText)
        bottomAppBar.setBackgroundColor(Cyanea.instance.primary)

        showHideButtons()
    }

    /**
     * Does not care about error
     */
    private fun updateInfoLine(){
        infoLine.text = viewModel.weekPosition.let {
                    when{
                        it == RozvrhViewModel.PERM -> getString(R.string.info_permanent)
                        it == 0 -> getString(R.string.info_this_week)
                        it == 1 -> getString(R.string.info_next_week)
                        it == -1 -> getString(R.string.info_last_week)
                        it < -1 -> resources.getQuantityString(R.plurals.info_weeks_back, -it, -it)
                        it > 1 -> resources.getQuantityString(R.plurals.info_weeks_forward, it, it)
                        else -> ""
                    }
                }
        val status: RozvrhStatus = viewModel.getStatusLD().value ?: RozvrhStatus.unknown()
        if (viewModel.isOfflineLD.value != false){
            if ((viewModel.showError || viewModel.getDisplayLD().value == null) && status.errMessage != null){
                infoLine.text = getString(status.errMessage)
            }else{
                infoLine.text = getString(R.string.info_offline, infoLine.text)
            }
        }
    }

    fun jumpToWeek(week: Int){
        viewModel.weekPosition = week
        showHideButtons()
    }

    /**
     * shows/hides buttons accordingly to current state.
     */
    private fun showHideButtons() {
        if (viewModel.weekPosition == 0) {
            ibPermanent.visibility = View.VISIBLE
            ibCurrent.visibility = View.GONE
        } else {
            ibPermanent.visibility = View.GONE
            ibCurrent.visibility = View.VISIBLE
        }
        if (viewModel.weekPosition == RozvrhViewModel.PERM) {
            ibNext.visibility = View.GONE
            ibPrev.visibility = View.GONE
        } else {
            ibPrev.visibility = View.VISIBLE
            ibNext.visibility = View.VISIBLE
        }
    }

}
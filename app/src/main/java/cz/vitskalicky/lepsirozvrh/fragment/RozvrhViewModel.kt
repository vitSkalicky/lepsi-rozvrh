package cz.vitskalicky.lepsirozvrh.fragment

import android.app.Application
import androidx.lifecycle.*
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatus
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import org.joda.time.LocalDate

class RozvrhViewModel(
        application: Application,
) : AndroidViewModel(application) {
    private val repository = getApplication<MainApplication>().repository
    private val displayLD: MediatorLiveData<RozvrhRelated> = MediatorLiveData()
    private val statusLD: MediatorLiveData<RozvrhStatus> = MediatorLiveData()

    private var currentlyUsedLD: LiveData<RozvrhRelated>? = null
    private var currentlyUsedStatusLD: LiveData<RozvrhStatus>? = null

    //to make switching instant
    private var nextLD: LiveData<RozvrhRelated>? = null
    private var nextStatusLD: LiveData<RozvrhStatus>? = null
    private var prevLD: LiveData<RozvrhRelated>? = null
    private var prevStatusLD: LiveData<RozvrhStatus>? = null
    private var permLD: LiveData<RozvrhRelated>? = null
    private var permStatusLD: LiveData<RozvrhStatus>? = null
    private var thisWeekLD: LiveData<RozvrhRelated>? = null
    private var thisWeekStatusLD: LiveData<RozvrhStatus>? = null

    /**
     * Tells if the last request was successful. If not, infoline should show "offline" on all weeks.
     */
    public val isOfflineLD: LiveData<Boolean> = repository.getOfflineStatusLiveData()

    fun getDisplayLD(): LiveData<RozvrhRelated> = displayLD
    fun getStatusLD(): LiveData<RozvrhStatus> = statusLD

    var monday: LocalDate = Utils.getCurrentMonday()
    private set

    /**
     * If loading rozvrh fails, but there is one in cache, hen show the user a messege that he/she is offline. But when the user refreshed then show the true error.
     */
    var showError: Boolean = false

    private fun weekToMonday(week: Int): LocalDate = if(week == PERM) {
        Rozvrh.PERM
    }else{
        Utils.getCurrentMonday().plusWeeks(week)
    }

    /**
     * Loads the LiveData and triggers data load so that it has a value ready to be instantly displayed. todo replace with a better method if you find any.
     */
    private fun prepareLD(week: Int): Pair<LiveData<RozvrhRelated>, LiveData<RozvrhStatus>>{
        val mnd = weekToMonday(week)
        val rozvrhLD = repository.getRozvrhLive(mnd, true)
        val statusLD = repository.getRozvrhStatusLiveData(mnd);

        //we must observe the live data to load the value. Observer is removed as soon as it receives any meaningful data.
        var rozvrhObserver = Observer<RozvrhRelated?> { }
        var statusObserver = Observer<RozvrhStatus?> { };

        statusObserver = Observer {
            if (it?.status == RozvrhStatus.Status.ERROR){
                statusLD.removeObserver(statusObserver)
                rozvrhLD.removeObserver(rozvrhObserver)
            }
        }
        rozvrhObserver = Observer {
            if (it != null){
                statusLD.removeObserver(statusObserver)
                rozvrhLD.removeObserver(rozvrhObserver)
            }
        }

        //todo this could potentially cause some memory leaks (in case the code above does not remove observers as intended). Would be nice to find a better solution to loading data from database directly into memory.
        rozvrhLD.observeForever(rozvrhObserver)
        statusLD.observeForever(statusObserver)

        return Pair(rozvrhLD, statusLD)
    }

    /**
     *  0 = current week, 1 = next week, -1 = previous week, [Int.MIN_VALUE] = permanent
     */
    var weekPosition: Int = 0
    set(value) {
        val old = field
        val diff = value - old;
        field = value;
        monday = weekToMonday(value)

        currentlyUsedLD?.let {
            displayLD.removeSource(it)
            displayLD.value = null
        }
        currentlyUsedStatusLD?.let {
            statusLD.removeSource(it)
            statusLD.value = RozvrhStatus.loading()
        }

        if (diff == 1){
            //shift the livedata
            prevLD = currentlyUsedLD
            prevStatusLD = currentlyUsedStatusLD
            currentlyUsedLD = nextLD ?: repository.getRozvrhLive(monday, true)
            currentlyUsedStatusLD = nextStatusLD ?: repository.getRozvrhStatusLiveData(monday)
            val nextLDs = prepareLD(field + 1)
            nextLD = nextLDs.first
            nextStatusLD = nextLDs.second
        } else if (diff == -1){
            //shift the livedata
            nextLD = currentlyUsedLD
            nextStatusLD = currentlyUsedStatusLD
            currentlyUsedLD = prevLD ?: repository.getRozvrhLive(monday, true)
            currentlyUsedStatusLD = prevStatusLD ?: repository.getRozvrhStatusLiveData(monday)
            val prevLDs = prepareLD(field - 1)
            prevLD = prevLDs.first
            prevStatusLD = prevLDs.second
        } else {

            if (field == 0){
                //jumped to current week
                currentlyUsedLD = thisWeekLD
                currentlyUsedStatusLD = thisWeekStatusLD
            }else if (field == PERM){
                currentlyUsedLD = permLD
                currentlyUsedStatusLD = permStatusLD
            }else{
                currentlyUsedLD = repository.getRozvrhLive(monday, true)
                currentlyUsedStatusLD = repository.getRozvrhStatusLiveData(monday)
            }

            if (field != PERM){
                val prevLDs = prepareLD(field - 1)
                prevLD = prevLDs.first
                prevStatusLD = prevLDs.second
                val nextLDs = prepareLD( field + 1)
                nextLD = nextLDs.first
                nextStatusLD = nextLDs.second
            }
        }

        //soft refresh in case the data has expired (there is no expiration check when just switching live data)
        repository.refresh(monday,true, force = false)
        if (field != PERM){
            repository.refresh(monday.plusWeeks(1),true, force = false)
            repository.refresh(monday.plusWeeks(-1),true, force = false)
        }


        displayLD.addSource(currentlyUsedLD!!) {displayLD.value = it}
        statusLD.addSource(currentlyUsedStatusLD!!) {statusLD.value = it}

        showError = false
    }

    fun forceRefresh(){
        showError = true
        repository.refresh(monday, true, true, true)
    }

    init {
        val thisWeekLDs = prepareLD( 0)
        thisWeekLD = thisWeekLDs.first
        thisWeekStatusLD = thisWeekLDs.second
        val permLDs = prepareLD( PERM)
        permLD = permLDs.first
        permStatusLD = permLDs.second

        weekPosition = weekPosition
    }

    companion object{
        const val PERM: Int = Int.MIN_VALUE
    }
}


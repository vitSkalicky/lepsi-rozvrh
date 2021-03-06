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

    /**
     *  0 = current week, 1 = next week, -1 = previous week, [Int.MIN_VALUE] = permanent
     */
    var weekPosition: Int = 0
    set(value) {
        field = value;
        monday = if(value == PERM) {
            Rozvrh.PERM
        }else{
            Utils.getCurrentMonday().plusWeeks(value)
        }

        currentlyUsedLD?.let {
            displayLD.removeSource(it)
            //todo: do this when an animation is added displayLD.value = null
        }
        currentlyUsedStatusLD?.let {
            statusLD.removeSource(it)
            //todo: do this when an animation is added statusLD.value = RozvrhStatus.loading()
        }
        currentlyUsedLD = repository.getRozvrhLive(monday, true)
        currentlyUsedStatusLD = repository.getRozvrhStatusLiveData(monday)
        displayLD.addSource(currentlyUsedLD!!) {displayLD.value = it}
        statusLD.addSource(currentlyUsedStatusLD!!) {statusLD.value = it}

        showError = false

        //prefetch next and prev
        if (field != PERM) {
            repository.refresh(monday.plusWeeks(1), false, false, )
            repository.refresh(monday.minusWeeks(1), false, false)
        }
        if (value == 0)
            repository.refresh(Rozvrh.PERM,false, false)
    }

    fun forceRefresh(){
        showError = true
        repository.refresh(monday, true,true)
    }

    init {
        weekPosition = weekPosition
    }

    companion object{
        const val PERM: Int = Int.MIN_VALUE
    }
}


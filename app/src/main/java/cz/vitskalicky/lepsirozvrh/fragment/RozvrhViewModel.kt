package cz.vitskalicky.lepsirozvrh.fragment

import android.app.Application
import androidx.lifecycle.*
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import io.sentry.util.Util
import org.joda.time.LocalDate

class RozvrhViewModel(
        application: Application,
) : AndroidViewModel(application) {
    private val repository = getApplication<MainApplication>().repository
    private val displayLD: MediatorLiveData<RozvrhRelated> = MediatorLiveData()

    private var currentlyUsedLD: LiveData<RozvrhRelated>? = null

    fun getDisplayLD(): LiveData<RozvrhRelated> = displayLD

    var monday: LocalDate = Utils.getCurrentMonday()
    private set

    /**
     *  0 = current week, 1 = next week, -1 = previous week, [Int.MIN_VALUE] = permanent
     */
    var weekPosition: Int = 0
    set(value) {
        field = value;
        monday = if(value == Int.MIN_VALUE) {
            Rozvrh.PERM
        }else{
            Utils.getCurrentMonday().plusWeeks(value)
        }

        currentlyUsedLD?.let {
            displayLD.removeSource(it)
        }
        displayLD.addSource(repository.getRozvrhLive(monday)) {displayLD.value = it}
    }
}


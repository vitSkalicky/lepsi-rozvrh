package cz.vitskalicky.lepsirozvrh.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import org.joda.time.LocalDate

class RozvrhStatusStore {
    private val map = HashMap<LocalDate, RozvrhStatus>()
    private val liveDatas = HashMap<LocalDate, MutableLiveData<RozvrhStatus>>()
    val isOffline = MutableLiveData<Boolean>(false)

    fun getLiveData(key: LocalDate): LiveData<RozvrhStatus> = liveDatas.getOrPut(key) {
        val ld = MutableLiveData<RozvrhStatus>()
        ld.value = get(key)
        return@getOrPut ld
    }

    operator fun get(key:LocalDate): RozvrhStatus = map[key] ?: RozvrhStatus.unknown()

    operator fun set(key: LocalDate, value: RozvrhStatus){
        map[key] = value
        liveDatas[key]?.value = value
    }

    fun clear(){
        map.clear()
        liveDatas.forEach{
            it.value.value = null
        }
    }
}
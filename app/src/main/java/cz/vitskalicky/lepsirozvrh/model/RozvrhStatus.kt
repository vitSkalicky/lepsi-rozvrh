package cz.vitskalicky.lepsirozvrh.model

import androidx.annotation.StringRes
import cz.vitskalicky.lepsirozvrh.R
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.model.RozvrhStatus.Status.*

data class RozvrhStatus(
    val status: Status,
    @StringRes
    val errMessage: Int? = null,
){
    companion object{
        fun success(): RozvrhStatus = RozvrhStatus(SUCCESS)
        fun loading(): RozvrhStatus = RozvrhStatus(LOADING)
        fun unknown(): RozvrhStatus = RozvrhStatus(UNKNOWN)

        fun unreachable(): RozvrhStatus = RozvrhStatus(ERROR, R.string.info_unreachable)
        fun loginFailed(): RozvrhStatus = RozvrhStatus(ERROR, R.string.info_login_failed)
        fun unexpectedResponse(): RozvrhStatus = RozvrhStatus(ERROR, R.string.info_unexpected_response)
    }

    fun asResource(rozvrh: RozvrhRelated?): Resource<RozvrhRelated>{
        val resourceStatus: Resource.Status = when(status){
            SUCCESS -> Resource.Status.SUCCESS
            LOADING -> Resource.Status.LOADING
            ERROR -> Resource.Status.ERROR
            UNKNOWN -> if (rozvrh == null) Resource.Status.ERROR else Resource.Status.SUCCESS
        }
        return Resource(resourceStatus, rozvrh, errMessage)
    }

    enum class Status{
        /**
         * Last refresh was successful
         */
        SUCCESS,

        /**
         * A new rozvrh is being loaded, but an old one might be available in database
         */
        LOADING,

        /**
         * Failed to download a new rozvrh, but an old one might be available in database
         */
        ERROR,

        /**
         * This schedule has not been interacted with in this instance of the app. Some older schedule might be available in database
         */
        UNKNOWN,
    }
}

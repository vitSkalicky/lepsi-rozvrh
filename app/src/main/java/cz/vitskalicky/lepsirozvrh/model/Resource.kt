package cz.vitskalicky.lepsirozvrh.model

import androidx.annotation.StringRes
import cz.vitskalicky.lepsirozvrh.model.Resource.Status.*

/**
 * A generic class that holds a value with its loading status.
 */
data class Resource<out T>(val status: Status, val data: T?, @StringRes val message: Int? = null) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(SUCCESS, data)
        }

        fun <T> error(@StringRes msg: Int, data: T?): Resource<T> {
            return Resource(ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(LOADING, data)
        }
    }
    enum class Status{
        SUCCESS,
        LOADING,
        ERROR
    }
}


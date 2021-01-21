package cz.vitskalicky.lepsirozvrh.bakaAPI.login

import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.SharedPrefs
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.Login.LoginResult.*
import io.sentry.Sentry
import kotlinx.coroutines.runBlocking
import okhttp3.*

class TokenAuthenticator(val app: MainApplication) : Authenticator, Interceptor {
    private val sprefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)

    override fun authenticate(route: Route?, response: Response): Request? {
        val origRequest: Request = response.request()
        val retried: Int = origRequest.tag(Retried::class.java)?.count ?: 0
        if (retried > 1) {
            return null
        }
        val usedAccessToken: String? = origRequest.header("Authorization")?.removePrefix("Bearer ")
        synchronized(app) {
            var currentAccessToken: String = sprefs.getString(SharedPrefs.ACCEESS_TOKEN, null) ?: ""
            if (usedAccessToken == currentAccessToken) {
                val refreshResult: Login.LoginResult = runBlocking {
                    app.login.refreshToken()
                }
                when (refreshResult) {
                    WRONG_LOGIN -> {
                        return null
                    }
                    UNREACHABLE, UNEXPECTED_RESPONSE -> {
                        return origRequest.newBuilder()
                                .tag(Retried::class.java, Retried(retried + 1))
                                .build()
                    }
                    SUCCESS -> {
                        currentAccessToken = sprefs.getString(SharedPrefs.ACCEESS_TOKEN, null) ?: ""
                    }
                }
            }
            return origRequest.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer " + currentAccessToken)
                    .tag(Retried::class.java, Retried(retried + 1))
                    .build()
        }
    }

    data class Retried(val count: Int)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token: String? = runBlocking {
            try {
                app.login.getAccessToken()
            } catch (_: LoginRequiredException) {
                null
            }
        }
        if (!token.isNullOrBlank()) {
            val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build()
            return chain.proceed(newRequest)
        }else{
            Log.w(TokenAuthenticator::class.simpleName, "Interceptor could not insert authentication header! access token is blank or empty")
        }
        return chain.proceed(chain.request())
    }
}
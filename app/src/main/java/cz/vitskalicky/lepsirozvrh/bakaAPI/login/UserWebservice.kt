package cz.vitskalicky.lepsirozvrh.bakaAPI.login

import retrofit2.http.GET

interface UserWebservice {

    @GET("api/3/user")
    suspend fun getUser(): UserResponse
}
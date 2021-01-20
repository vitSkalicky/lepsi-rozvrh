package cz.vitskalicky.lepsirozvrh.bakaAPI.login

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginWebservice {
    @FormUrlEncoded
    @POST("api/login")
    suspend fun firstLogin(@Field("username") username: String, @Field("password") password: String, @Field("client_id") clientId: String = "ANDR", @Field("grant_type") grantType: String = "password"): LoginResponse

    @FormUrlEncoded
    @POST("api/login")
    suspend fun refreshLogin(@Field("refresh_token") refreshToken: String, @Field("client_id") clientId: String = "ANDR", @Field("grant_type") grantType: String = "refresh_token"): LoginResponse

    @GET("api/3/user")
    suspend fun getUser(): UserResponse
}
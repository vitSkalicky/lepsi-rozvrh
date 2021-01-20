package cz.vitskalicky.lepsirozvrh.bakaAPI.login

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginError(
        val error: String,
        val error_description: String = ""
)
package cz.vitskalicky.lepsirozvrh.bakaAPI.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    public String access_token;
    public String token_type;
    public int expires_in;
    public String refresh_token;
}

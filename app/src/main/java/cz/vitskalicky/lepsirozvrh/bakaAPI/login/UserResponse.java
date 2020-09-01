package cz.vitskalicky.lepsirozvrh.bakaAPI.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    public String fullName;
    public String UserTypeText;
    public String UserType;
}

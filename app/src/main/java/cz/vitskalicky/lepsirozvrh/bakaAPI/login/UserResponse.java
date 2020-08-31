package cz.vitskalicky.lepsirozvrh.bakaAPI.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    String fullName;
    String UserTypeText;
    String UserType;
}

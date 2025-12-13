package com.wareland.user.dto;

public class LoginResponse {
    private String token;
    private UserProfileResponse profile;

    public LoginResponse() {}

    public LoginResponse(String token, UserProfileResponse profile) {
        this.token = token;
        this.profile = profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(UserProfileResponse profile) {
        this.profile = profile;
    }
}

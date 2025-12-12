package com.wareland.user.dto;

import jakarta.validation.constraints.Email;

public class UpdateProfileRequest {

    private String name;

    @Email
    private String email;

    private String phoneNumber;

    // Optional password update
    private String oldPassword;
    private String newPassword;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}

package com.wareland.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import com.wareland.common.validation.StrongPassword;

public class UpdateProfileRequest {

    private String name;

    @Email
    private String email;

    private String phoneNumber;

    // Optional password update
    private String oldPassword;
    @Size(min = 6, max = 100)
    @StrongPassword
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

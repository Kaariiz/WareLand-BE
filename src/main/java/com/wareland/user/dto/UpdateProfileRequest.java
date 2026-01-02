package com.wareland.user.dto;

import com.wareland.common.validation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO untuk request update profil user.
 * Seluruh field bersifat opsional.
 */
public class UpdateProfileRequest {

    private String name;

    @Email
    private String email;

    private String phoneNumber;

    @Size(max = 500)
    private String imageUrl;

    // Digunakan jika user ingin mengganti password
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}

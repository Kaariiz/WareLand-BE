package com.wareland.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wareland.common.response.ApiResponse;
import com.wareland.user.dto.UpdateProfileRequest;
import com.wareland.user.dto.UserProfileResponse;
import com.wareland.user.service.UserService;

import jakarta.validation.Valid;

/**
 * Controller untuk operasi user:
 * melihat data user, mengelola profil, dan menghapus akun.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Mengambil seluruh data user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getAllUsers())
        );
    }

    /**
     * Mengambil profil user berdasarkan ID.
     * (Saat ini belum dibatasi autentikasi)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable Long id
    ) {
        UserProfileResponse profile = userService.getProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Update profil user berdasarkan ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileResponse updated =
                userService.updateProfile(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Profil berhasil diperbarui", updated)
        );
    }

    /**
     * Menghapus akun user berdasarkan ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable Long id
    ) {
        userService.deleteAccount(id);
        return ResponseEntity.ok(
                ApiResponse.success("Akun berhasil dihapus", null)
        );
    }

    /**
     * Mengambil daftar user berdasarkan role.
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<?>> getUsersByRole(
            @PathVariable String role
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getUsersByRole(role))
        );
    }
}

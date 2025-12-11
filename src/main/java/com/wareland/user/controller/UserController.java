package com.wareland.user.controller;

import com.wareland.common.response.ApiResponse;
import com.wareland.user.dto.UpdateProfileRequest;
import com.wareland.user.dto.UserProfileResponse;
import com.wareland.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk operasi mandiri user:
 * lihat profil, update profil, dan hapus akun.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get profile by ID (untuk sekarang tanpa auth, bisa ditambahkan nanti)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable Long id) {
        UserProfileResponse profile = userService.getProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    // Update profile mandiri
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserProfileResponse updated = userService.updateProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Profil berhasil diperbarui", updated));
    }

    // Delete akun mandiri
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        userService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Akun berhasil dihapus", null));

    }
}

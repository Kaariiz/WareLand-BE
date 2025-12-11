package com.wareland.user.controller;

import com.wareland.common.response.ApiResponse;
import com.wareland.user.dto.LoginRequest;
import com.wareland.user.dto.UserProfileResponse;
import com.wareland.user.dto.UserRegisterRequest;
import com.wareland.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk endpoint autentikasi:
 * registrasi dan login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {

        UserProfileResponse profile = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registrasi berhasil", profile));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserProfileResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        UserProfileResponse profile = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login berhasil", profile));
    }
}

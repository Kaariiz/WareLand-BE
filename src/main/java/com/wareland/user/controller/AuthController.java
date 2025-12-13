package com.wareland.user.controller;

import com.wareland.common.response.ApiResponse;
import com.wareland.common.security.JwtTokenProvider;
import com.wareland.user.dto.LoginRequest;
import com.wareland.user.dto.LoginResponse;
import com.wareland.user.dto.UserProfileResponse;
import com.wareland.user.dto.UserRegisterRequest;
import com.wareland.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk endpoint autentikasi:
 * registrasi dan login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {

        UserProfileResponse profile = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registrasi berhasil", profile));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        UserProfileResponse profile = userService.login(request);
        String token = jwtTokenProvider.generateToken(profile.getUsername());
        LoginResponse response = new LoginResponse(token, profile);
        return ResponseEntity.ok(ApiResponse.success("Login berhasil", response));
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : null;
        UserProfileResponse profile = userService.getProfileByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}

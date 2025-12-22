package com.wareland.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wareland.common.response.ApiResponse;
import com.wareland.common.security.JwtTokenProvider;
import com.wareland.user.dto.LoginRequest;
import com.wareland.user.dto.LoginResponse;
import com.wareland.user.dto.UserProfileResponse;
import com.wareland.user.dto.UserRegisterRequest;
import com.wareland.user.service.AuthService;
import com.wareland.user.service.UserService;

import jakarta.validation.Valid;

/**
 * Controller untuk menangani proses autentikasi pengguna
 * (registrasi, login, logout, dan akses profil sendiri).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public AuthController(
            UserService userService,
            JwtTokenProvider jwtTokenProvider,
            AuthService authService
    ) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
    }

    /**
     * Endpoint registrasi user baru.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        UserProfileResponse profile = userService.register(request);
        return ResponseEntity.ok(
                ApiResponse.success("Registrasi berhasil", profile)
        );
    }

    /**
     * Endpoint login user dan pembuatan JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        UserProfileResponse profile = userService.login(request);
        String token = jwtTokenProvider.generateToken(profile.getUsername());

        LoginResponse response = new LoginResponse(token, profile);
        return ResponseEntity.ok(
                ApiResponse.success("Login berhasil", response)
        );
    }

    /**
     * Endpoint untuk mengambil profil user yang sedang login
     * berdasarkan SecurityContext.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username =
                authentication != null ? authentication.getName() : null;

        UserProfileResponse profile =
                userService.getProfileByUsername(username);

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Endpoint logout user (invalidasi token jika diperlukan).
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(name = "Authorization", required = false)
            String authorizationHeader
    ) {
        authService.logout(authorizationHeader);
        return ResponseEntity.ok(
                ApiResponse.success("Logout berhasil", null)
        );
    }
}

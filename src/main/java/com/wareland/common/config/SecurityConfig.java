package com.wareland.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.wareland.common.security.JwtAuthenticationFilter;

/**
 * Konfigurasi keamanan aplikasi WareLand menggunakan JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Nonaktifkan CSRF karena menggunakan stateless JWT
                .csrf(csrf -> csrf.disable())

                // Integrasi CORS dengan konfigurasi terpisah
                .cors(Customizer.withDefaults())

                // Tidak menggunakan session (stateless)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Aturan otorisasi endpoint
                .authorizeHttpRequests(auth -> auth
                        // Izinkan preflight request
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Endpoint publik autentikasi
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()
                        // Katalog properti bersifat publik
                        .requestMatchers("/api/catalog/**").permitAll()
                        // Endpoint lain wajib autentikasi
                        .anyRequest().authenticated()
                );

        // Filter JWT dijalankan sebelum UsernamePasswordAuthenticationFilter
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    /**
     * Password encoder default untuk hashing password user.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

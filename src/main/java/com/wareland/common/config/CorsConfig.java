package com.wareland.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Konfigurasi CORS untuk mengatur akses frontend ke backend WareLand.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origin frontend yang diizinkan (local & production)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://ware-land-fe.vercel.app"
        ));

        // Mengizinkan pengiriman Authorization header (JWT)
        config.setAllowCredentials(true);

        // HTTP method yang diizinkan
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        // Header yang diizinkan dari client
        config.setAllowedHeaders(
                List.of("Authorization", "Content-Type")
        );

        // Header yang dapat diakses oleh client
        config.setExposedHeaders(
                List.of("Authorization")
        );

        // Cache preflight request (detik)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}

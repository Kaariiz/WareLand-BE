package com.wareland.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Allowed origins: local Next.js and production FE (if exists)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://ware-land-fe.vercel.app"
        ));
        // Allow credentials so browser can send Authorization header
        config.setAllowCredentials(true);
        // Allowed methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allowed headers
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Expose Authorization header if needed by client
        config.setExposedHeaders(List.of("Authorization"));
        // Cache preflight response
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

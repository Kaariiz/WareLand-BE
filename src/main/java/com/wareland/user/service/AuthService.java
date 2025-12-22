package com.wareland.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wareland.user.model.RevokedToken;
import com.wareland.user.repository.RevokedTokenRepository;

/**
 * Service untuk menangani proses autentikasi tambahan
 * seperti logout dan manajemen token yang di-revoke.
 */
@Service
public class AuthService {

    private final RevokedTokenRepository revokedTokenRepository;

    public AuthService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    /**
     * Logout user dengan menyimpan token JWT ke daftar revoked token.
     */
    @Transactional
    public void logout(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return;
        }

        String token = bearerToken;

        // Ambil token asli jika menggunakan prefix "Bearer "
        if (bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }

        if (!token.isBlank() && !revokedTokenRepository.existsByToken(token)) {
            revokedTokenRepository.save(new RevokedToken(token));
        }
    }
}

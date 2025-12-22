package com.wareland.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wareland.user.model.RevokedToken;

/**
 * Repository untuk mengakses data token JWT yang telah di-revoke.
 */
public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, Long> {

    /**
     * Mengecek apakah token tertentu sudah di-revoke.
     */
    boolean existsByToken(String token);
}

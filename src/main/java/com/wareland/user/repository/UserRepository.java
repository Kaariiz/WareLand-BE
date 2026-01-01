package com.wareland.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wareland.user.model.User;

/**
 * Repository untuk mengelola data User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Mencari user berdasarkan username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Mencari user berdasarkan email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Mengecek keberadaan username.
     */
    boolean existsByUsername(String username);

    /**
     * Mengecek keberadaan email.
     */
    boolean existsByEmail(String email);

    /**
     * Mengambil daftar user berdasarkan role (menggunakan discriminator column).
     */
    @Query("SELECT u FROM User u WHERE TYPE(u) = :userType")
    List<User> findByType(@Param("userType") Class<? extends User> userType);
}

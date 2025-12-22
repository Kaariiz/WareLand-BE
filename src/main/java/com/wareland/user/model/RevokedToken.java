package com.wareland.user.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entity untuk menyimpan token JWT yang sudah di-revoke (logout).
 */
@Entity
@Table(
        name = "revoked_tokens",
        indexes = {
                @Index(
                        name = "idx_revoked_token_token",
                        columnList = "token",
                        unique = true
                )
        }
)
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private Instant revokedAt;

    protected RevokedToken() {
        // Constructor khusus untuk JPA
    }

    public RevokedToken(String token) {
        this.token = token;
        this.revokedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }
}

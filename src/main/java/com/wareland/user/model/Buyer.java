package com.wareland.user.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Entity Buyer sebagai turunan dari User.
 * Merepresentasikan user dengan peran pembeli.
 */
@Entity
@DiscriminatorValue("BUYER")
public class Buyer extends User {

    /**
     * Inisialisasi khusus saat proses registrasi buyer.
     */
    @Override
    public void register() {
        // Set role default sebagai BUYER
        setUserRole(UserRole.BUYER);
    }
}

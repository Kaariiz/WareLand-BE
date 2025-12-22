package com.wareland.user.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Entity Seller sebagai turunan dari User.
 * Merepresentasikan user dengan peran penjual.
 */
@Entity
@DiscriminatorValue("SELLER")
public class Seller extends User {

    /**
     * Inisialisasi khusus saat proses registrasi seller.
     */
    @Override
    public void register() {
        // Set role default sebagai SELLER
        setUserRole(UserRole.SELLER);
    }
}

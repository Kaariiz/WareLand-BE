package com.wareland.user.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import com.wareland.review.model.Review;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Buyer sebagai turunan dari User.
 * Merepresentasikan user dengan peran pembeli.
 */
@Entity
@DiscriminatorValue("BUYER")
public class Buyer extends User {

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    /**
     * Inisialisasi khusus saat proses registrasi buyer.
     */
    @Override
    public void register() {
        // Role sudah ditentukan oleh Hibernate discriminator @DiscriminatorValue("BUYER")
        // Tidak perlu set manual lagi
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}

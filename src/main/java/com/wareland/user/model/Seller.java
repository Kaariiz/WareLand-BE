package com.wareland.user.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import com.wareland.property.model.Property;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Seller sebagai turunan dari User.
 * Merepresentasikan user dengan peran penjual.
 */
@Entity
@DiscriminatorValue("SELLER")
public class Seller extends User {

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> properties = new ArrayList<>();

    /**
     * Inisialisasi khusus saat proses registrasi seller.
     */
    @Override
    public void register() {
        // Set role default sebagai SELLER
        setUserRole(UserRole.SELLER);
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}

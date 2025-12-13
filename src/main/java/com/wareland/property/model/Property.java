package com.wareland.property.model;

import com.wareland.user.model.Seller;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer propertyId;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String address;

    @Min(0)
    @Column(nullable = false)
    private double price;

    @Column(length = 2000)
    private String description;

    // TEPAT 1 owner (Seller). Aggregation via reference
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // Behavior: update basic details only (no ownership change)
    public void updateDetails(String newAddress, double newPrice, String newDescription) {
        if (newAddress != null && !newAddress.isBlank()) {
            this.address = newAddress;
        }
        if (newPrice >= 0) {
            this.price = newPrice;
        }
        if (newDescription != null) {
            this.description = newDescription;
        }
    }

    public String displayProperty() {
        return String.format("Property{id=%d, address='%s', price=%.2f}", propertyId, address, price);
    }

    // Getters & Setters (encapsulation)
    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }
}

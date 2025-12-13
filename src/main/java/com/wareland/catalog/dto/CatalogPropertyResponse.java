package com.wareland.catalog.dto;

/**
 * DTO read-only untuk katalog properti publik.
 */
public class CatalogPropertyResponse {

    private int propertyId;
    private String address;
    private double price;
    private String description;

    public CatalogPropertyResponse() {
    }

    public CatalogPropertyResponse(int propertyId, String address, double price, String description) {
        this.propertyId = propertyId;
        this.address = address;
        this.price = price;
        this.description = description;
    }

    public int getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(int propertyId) {
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
}

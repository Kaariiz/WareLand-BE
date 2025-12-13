package com.wareland.catalog.dto;

/**
 * DTO parameter pencarian katalog (semua optional).
 */
public class CatalogSearchRequest {

    private String keyword; // optional
    private Double minPrice; // optional
    private Double maxPrice; // optional

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
}

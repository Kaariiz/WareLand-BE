package com.wareland.review.dto;

import jakarta.validation.constraints.*;

public class ReviewCreateRequest {

    @NotNull
    private Long buyerId;

    @NotNull
    private Long propertyId;

    @Min(1)
    @Max(5)
    private int rating;

    @NotBlank
    private String comment;

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

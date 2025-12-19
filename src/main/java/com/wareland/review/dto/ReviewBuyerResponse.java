package com.wareland.review.dto;

import java.time.LocalDateTime;

public class ReviewBuyerResponse {

    private Long reviewId;
    private Long propertyId;
    private String propertyTitle;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewBuyerResponse(Long reviewId, Long propertyId, String propertyTitle, int rating, String comment, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.propertyId = propertyId;
        this.propertyTitle = propertyTitle;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

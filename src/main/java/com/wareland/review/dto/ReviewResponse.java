package com.wareland.review.dto;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long reviewId;
    private int rating;
    private String comment;
    private String buyerName;
    private LocalDateTime createdAt;

    public ReviewResponse(Long reviewId, int rating, String comment, String buyerName, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.comment = comment;
        this.buyerName = buyerName;
        this.createdAt = createdAt;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

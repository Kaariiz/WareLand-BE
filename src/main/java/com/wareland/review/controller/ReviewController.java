package com.wareland.review.controller;

import com.wareland.common.response.ApiResponse;
import com.wareland.review.dto.ReviewCreateRequest;
import com.wareland.review.dto.ReviewResponse;
import com.wareland.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // POST → membuat review (buyer only via service validation)
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.ok(ApiResponse.success("Review berhasil dibuat", response));
    }

    // GET → melihat seluruh review dari suatu properti
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByProperty(@PathVariable("propertyId") Long propertyId) {
        List<ReviewResponse> responses = reviewService.getReviewsByProperty(propertyId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}

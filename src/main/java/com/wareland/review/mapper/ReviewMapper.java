package com.wareland.review.mapper;

import com.wareland.review.dto.ReviewResponse;
import com.wareland.review.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        String buyerName = review.getBuyer() != null ? review.getBuyer().getName() : null;
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                buyerName,
                review.getCreatedAt()
        );
    }
}

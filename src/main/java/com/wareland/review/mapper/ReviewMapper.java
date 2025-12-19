package com.wareland.review.mapper;

import com.wareland.review.dto.ReviewBuyerResponse;
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

    public ReviewBuyerResponse toBuyerResponse(Review review) {
        Long propertyId = review.getProperty() != null && review.getProperty().getPropertyId() != null
                ? review.getProperty().getPropertyId().longValue() : null;
        // Dalam model saat ini belum ada field "title" pada Property, gunakan address sebagai judul ringkas
        String propertyTitle = review.getProperty() != null ? review.getProperty().getAddress() : null;
        return new ReviewBuyerResponse(
                review.getId(),
                propertyId,
                propertyTitle,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}

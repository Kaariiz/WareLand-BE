package com.wareland.review.repository;

import com.wareland.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBuyerUserIdAndPropertyPropertyId(Long buyerId, Integer propertyId);

    List<Review> findAllByPropertyPropertyIdOrderByCreatedAtDesc(Integer propertyId);
}

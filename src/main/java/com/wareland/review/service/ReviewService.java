package com.wareland.review.service;

import com.wareland.common.exception.BusinessException;
import com.wareland.common.exception.ResourceNotFoundException;
import com.wareland.property.model.Property;
import com.wareland.property.repository.PropertyRepository;
import com.wareland.review.dto.ReviewCreateRequest;
import com.wareland.review.dto.ReviewResponse;
import com.wareland.review.mapper.ReviewMapper;
import com.wareland.review.model.Review;
import com.wareland.review.repository.ReviewRepository;
import com.wareland.user.model.Buyer;
import com.wareland.user.model.User;
import com.wareland.user.model.UserRole;
import com.wareland.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         PropertyRepository propertyRepository,
                         ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.reviewMapper = reviewMapper;
    }

    public ReviewResponse createReview(ReviewCreateRequest request) {
        // Validasi rating dan comment di service untuk memastikan business rule
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException("Rating harus antara 1 hingga 5");
        }
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new BusinessException("Comment tidak boleh kosong");
        }

        User user = userRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer dengan ID " + request.getBuyerId() + " tidak ditemukan"));

        // Pastikan hanya Buyer yang dapat membuat review
        if (user.getUserRole() != UserRole.BUYER || !(user instanceof Buyer)) {
            throw new BusinessException("Hanya buyer yang dapat membuat review");
        }
        Buyer buyer = (Buyer) user;

        Property property = propertyRepository.findById(Math.toIntExact(request.getPropertyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Property dengan ID " + request.getPropertyId() + " tidak ditemukan"));

        // Cek duplikasi: satu buyer hanya boleh satu review untuk properti yang sama
        boolean exists = reviewRepository.existsByBuyerUserIdAndPropertyPropertyId(
                buyer.getUserId(), property.getPropertyId());
        if (exists) {
            throw new BusinessException("Anda sudah memberikan review untuk properti ini");
        }

        Review review = new Review();
        review.setBuyer(buyer);
        review.setProperty(property);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        return reviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProperty(Long propertyId) {
        // propertyId dalam entity Property bertipe Integer
        Integer pid = Math.toIntExact(propertyId);
        List<Review> reviews = reviewRepository.findAllByPropertyPropertyIdOrderByCreatedAtDesc(pid);
        return reviews.stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }
}

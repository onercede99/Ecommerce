package com.codegym.service;

import com.codegym.dto.ReviewDto;
import com.codegym.model.Product;
import com.codegym.model.Review;
import com.codegym.model.User;
import com.codegym.repository.ProductRepository;
import com.codegym.repository.ReviewRepository;
import com.codegym.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void addReview(Long productId, ReviewDto reviewDto) {
        // Lấy người dùng đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));


        if (!reviewRepository.hasUserPurchasedProduct(productId, user.getId())) {
            throw new IllegalStateException("Bạn chỉ có thể đánh giá các sản phẩm bạn đã đặt hàng.");
        }

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này rồi.");
        }
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        reviewRepository.save(review);
    }
}
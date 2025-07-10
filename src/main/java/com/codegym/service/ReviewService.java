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

        // Lấy sản phẩm
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // KIỂM TRA LOGIC NGHIỆP VỤ
        // 1. Kiểm tra xem người dùng đã mua sản phẩm này chưa
        if (!reviewRepository.hasUserPurchasedProduct(productId, user.getId())) {
            throw new IllegalStateException("You can only review products you have purchased and received.");
        }

        // 2. Kiểm tra xem người dùng đã đánh giá sản phẩm này trước đó chưa
        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new IllegalStateException("You have already reviewed this product.");
        }

        // Tạo và lưu đánh giá mới
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        reviewRepository.save(review);
    }
}
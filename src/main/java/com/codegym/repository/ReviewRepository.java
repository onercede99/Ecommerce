// File: src/main/java/com/codegym/repository/ReviewRepository.java
package com.codegym.repository;

import com.codegym.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Tìm tất cả review của một sản phẩm, có phân trang, sắp xếp theo ngày mới nhất
    Page<Review> findByProductIdOrderByReviewDateDesc(Long productId, Pageable pageable);

    // Dùng query này để lấy thông tin tổng hợp (trung bình sao và tổng số review)
    @Query("SELECT new com.codegym.dto.ReviewSummaryDto(AVG(r.rating), COUNT(r.id)) FROM Review r WHERE r.product.id = :productId")
    com.codegym.dto.ReviewSummaryDto getReviewSummary(Long productId);
}
package com.codegym.repository;

import com.codegym.dto.ReviewSummaryDto;
import com.codegym.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tìm tất cả các đánh giá của một sản phẩm, sắp xếp theo ngày mới nhất
    @Query(value = "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.product.id = :productId",
            countQuery = "SELECT count(r) FROM Review r WHERE r.product.id = :productId")
    Page<Review> findByProductIdWithUser(Long productId, Pageable pageable);

    // (Nâng cao) Dùng để tính toán tóm tắt số sao và tổng số đánh giá
    // Chúng ta sẽ tạo một DTO cho việc này sau
    @Query("SELECT new com.codegym.dto.ReviewSummaryDto(AVG(r.rating), COUNT(r.id)) FROM Review r WHERE r.product.id = :productId")
    ReviewSummaryDto getReviewSummary(Long productId);

    // Kiểm tra xem một người dùng đã từng đánh giá sản phẩm này chưa
    boolean existsByProductIdAndUserId(Long productId, Long userId);

    // (Nâng cao) Kiểm tra xem người dùng đã từng mua sản phẩm này chưa
    // Cần một câu query phức tạp hơn để kiểm tra trong bảng orders và order_details
    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od " +
            "WHERE od.product.id = :productId AND od.order.user.id = :userId")
    boolean hasUserPurchasedProduct(Long productId, Long userId);
}
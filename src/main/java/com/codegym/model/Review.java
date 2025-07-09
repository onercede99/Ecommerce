package com.codegym.model;

import lombok.Getter;
import lombok.Setter;

// THAY ĐỔI: import từ javax, không phải jakarta
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter // THÊM: Lombok tự tạo getter
@Setter // THÊM: Lombok tự tạo setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int rating; // Điểm đánh giá từ 1-5

    @Lob
    private String comment;

    @Column(nullable = false)
    private LocalDateTime reviewDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.reviewDate = LocalDateTime.now();
    }

}
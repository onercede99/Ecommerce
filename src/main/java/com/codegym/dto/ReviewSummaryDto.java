package com.codegym.dto;

public class ReviewSummaryDto {
    private Double averageRating;
    private Long totalReviews;

    public ReviewSummaryDto(Double averageRating, Long totalReviews) {
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
    }

    public Double getAverageRating() { return averageRating; }
    public Long getTotalReviews() { return totalReviews; }
}
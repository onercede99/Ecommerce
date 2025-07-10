package com.codegym.service;

import com.codegym.dto.ReviewDto;

public interface IReviewService {
    void addReview(Long productId, ReviewDto reviewDto);
}
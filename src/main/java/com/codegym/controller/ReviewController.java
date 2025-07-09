// File: src/main/java/com/codegym/controller/ReviewController.java
package com.codegym.controller;

import com.codegym.model.*;
import com.codegym.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewController {
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewController(ReviewRepository reviewRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/reviews/add")
    public String addReview(@RequestParam Long productId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Received review submission for product ID: {}", productId);


        if (userDetails == null) {
            logger.warn("User is not authenticated. Redirecting to login.");
            return "redirect:/login";
        }
        logger.info("User '{}' is submitting a review.", userDetails.getUsername());


        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);

        reviewRepository.save(review);
        logger.info("Review saved successfully for product ID: {}", productId);


        return "redirect:/products/" + productId;
    }
}
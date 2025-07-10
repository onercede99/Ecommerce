package com.codegym.controller;

import com.codegym.dto.ReviewDto;
import com.codegym.service.IReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class ReviewController {

    private final IReviewService reviewService;

    public ReviewController(IReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews/add/{productId}")
    public String addReview(@PathVariable Long productId,
                            @Valid @ModelAttribute("newReview") ReviewDto reviewDto,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {

        if(result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newReview", result);
            redirectAttributes.addFlashAttribute("newReview", reviewDto);

            // Thêm một thông báo lỗi chung
            redirectAttributes.addFlashAttribute("reviewError", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại các trường được báo đỏ.");

            // Chuyển hướng về lại đúng trang sản phẩm
            return "redirect:/products/" + productId;
        }

        try {
            reviewService.addReview(productId, reviewDto);
            redirectAttributes.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã gửi đánh giá!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewError", "Đã có lỗi xảy ra. Vui lòng thử lại.");
        }

        return "redirect:/products/" + productId;
    }
}
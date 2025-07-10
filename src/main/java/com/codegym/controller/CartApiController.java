 package com.codegym.controller;

import com.codegym.service.ICartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/cart") // Đường dẫn cơ sở cho các API giỏ hàng
public class CartApiController {

    private final ICartService cartService;

    public CartApiController(ICartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam("productId") Long productId,
                                                         @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                                                         HttpSession session) {
        try {
            cartService.addToCart(productId, quantity, session);

            // Lấy tổng số lượng sản phẩm mới trong giỏ hàng
            int totalItems = cartService.getTotalItems(session);

            // Trả về một đối tượng JSON chứa số lượng mới
            return ResponseEntity.ok(Map.of("totalItems", totalItems));

        } catch (IllegalArgumentException e) {
            // Trường hợp không tìm thấy sản phẩm
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
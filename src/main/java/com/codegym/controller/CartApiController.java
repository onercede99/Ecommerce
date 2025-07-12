package com.codegym.controller;

import com.codegym.dto.CartDto;
import com.codegym.service.ICartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final ICartService cartService;

    public CartApiController(ICartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam("productId") Long productId,
                                                         @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                                                         HttpSession session) {
        // ... giữ nguyên ...
        try {
            cartService.addToCart(productId, quantity, session);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Sản phẩm đã được thêm vào giỏ hàng!");
            response.put("totalItems", cartService.getTotalItems(session));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== SỬA LỖI: CHUYỂN @PutMapping THÀNH @PostMapping =====
    @PostMapping("/update") // Đổi từ @PutMapping
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestParam("productId") Long productId,
                                                              @RequestParam("quantity") int quantity,
                                                              HttpSession session) {
        cartService.updateCart(productId, quantity, session);
        return createCartResponse(session);
    }

    // ===== SỬA LỖI: CHUYỂN @DeleteMapping THÀNH @PostMapping =====
    @PostMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeCartItem(@PathVariable("productId") Long productId,
                                                              HttpSession session) {
        cartService.removeFromCart(productId, session);
        return createCartResponse(session);
    }

    // ===== SỬA LỖI: CHUYỂN @DeleteMapping THÀNH @PostMapping =====
    @PostMapping("/clear") // Đổi từ @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        cartService.clearCart(session);
        return createCartResponse(session);
    }

    // Hàm helper giữ nguyên
    private ResponseEntity<Map<String, Object>> createCartResponse(HttpSession session) {
        CartDto cart = cartService.getCart(session);
        Map<String, Object> response = new HashMap<>();
        response.put("totalItems", cart.getTotalItems());
        response.put("totalPrice", cart.getTotalPrice());
        response.put("formattedTotalPrice", String.format("%,.0f ₫", cart.getTotalPrice()));
        response.put("items", cart.getItems().values());
        return ResponseEntity.ok(response);
    }
}
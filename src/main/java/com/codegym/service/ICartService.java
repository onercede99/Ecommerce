package com.codegym.service;

import com.codegym.dto.CartDto;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;

public interface ICartService {
    void addToCart(Long productId, int quantity, HttpSession session);
    void updateCart(Long productId, int quantity, HttpSession session);
    void removeFromCart(Long productId, HttpSession session);
    void clearCart(HttpSession session);

    // Phương thức này sẽ tự động quyết định lấy từ session hay CSDL
    CartDto getCart(HttpSession session);
    int getTotalItems(HttpSession session);
    BigDecimal getTotalPrice(HttpSession session);

    // Phương thức quan trọng để hợp nhất
    void mergeSessionCartWithDbCart(HttpSession session);
}

package com.codegym.service;

import com.codegym.dto.CartDto;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;

public interface ICartService {

    void addToCart(Long productId, int quantity, HttpSession session);

    void updateCart(Long productId, int quantity, HttpSession session);

    void removeFromCart(Long productId, HttpSession session);

    void clearCart(HttpSession session);

    // Phương thức chính để lấy giỏ hàng, dùng cho cả Controller và API
    CartDto getCart(HttpSession session);

    // Phương thức này được gọi bởi GlobalControllerAdvice
    int getTotalItems(HttpSession session);

    // Phương thức này có thể hữu ích trong tương lai
    BigDecimal getTotalPrice(HttpSession session);

    void mergeSessionCartWithDbCart(HttpSession session);
}
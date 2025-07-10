package com.codegym.service;

import com.codegym.dto.CartDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;

public interface ICartService {
    void addToCart(Long productId, int quantity, HttpSession session);
    void updateCart(Long productId, int quantity, HttpSession session);
    void removeFromCart(Long productId, HttpSession session);
    void clearCart(HttpSession session);
    CartDto getCart(HttpSession session);
    int getTotalItems(HttpSession session);
    BigDecimal getTotalPrice(HttpSession session);

    CartDto getCart(HttpServletRequest request);
    void mergeSessionCartWithDbCart(HttpSession session);
}

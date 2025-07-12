package com.codegym.controller;

import com.codegym.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    private ICartService cartService;

    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpServletRequest request) {
        return cartService.getTotalItems(request.getSession());
    }
}
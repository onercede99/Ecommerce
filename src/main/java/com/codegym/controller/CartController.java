package com.codegym.controller;

import com.codegym.service.ICartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            HttpServletRequest request) { // Dùng HttpServletRequest để lấy session
        cartService.addToCart(productId, quantity, request.getSession());
        return "redirect:/products";
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        model.addAttribute("cart", cartService.getCart(session));
        return "cart/view";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam("productId") Long productId,
                             @RequestParam("quantity") int quantity, HttpSession session) {
        cartService.updateCart(productId, quantity, session);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable("productId") Long productId, HttpSession session) {
        cartService.removeFromCart(productId, session);
        return "redirect:/cart";
    }
}

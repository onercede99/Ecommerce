package com.codegym.controller;

import com.codegym.model.Order;
import com.codegym.service.ICartService;
import com.codegym.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final ICartService cartService;

    public OrderController(OrderService orderService, ICartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    // 1. Hiển thị trang checkout
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session) { // Thêm HttpSession
        // Gọi getCart với session
        if (cartService.getCart(session).getItems().isEmpty()) {
            return "redirect:/cart";
        }
        // Gọi getCart với session
        model.addAttribute("cart", cartService.getCart(session));
        return "order/checkout";
    }

    // 2. Xử lý đặt hàng
    @PostMapping("/order/place")
    public String placeOrder(@RequestParam String customerName,
                             @RequestParam String shippingAddress,
                             @RequestParam String phoneNumber,
                             @RequestParam String email,
                             @RequestParam(required = false) String notes,
                             Model model,
                             HttpSession session) { // Thêm HttpSession

        if (cartService.getCart(session).getItems().isEmpty()) {
            return "redirect:/cart";
        }

        Order order = orderService.createOrder(customerName, shippingAddress, phoneNumber, email, notes, session);

        model.addAttribute("orderId", order.getId());

        return "order/success";
    }
}
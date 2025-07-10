package com.codegym.controller;

import com.codegym.model.Order;
import com.codegym.repository.OrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository orderRepository;

    public AdminOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }



    @GetMapping
    public String listOrders(Model model) {
        List<Order> orders = orderRepository.findAllWithUserDetails(Sort.by(Sort.Direction.DESC, "orderDate"));
        model.addAttribute("orders", orders);
        return "admin/order/list";
    }

    // 2. Hiển thị chi tiết một đơn hàng
    @GetMapping("/{id}")
    public String viewOrderDetail(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
            model.addAttribute("order", order);
            return "admin/order/detail";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    // 3. (Nâng cao) Cập nhật trạng thái đơn hàng
    @PostMapping("/update-status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") String status,
                                    RedirectAttributes ra) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

            order.setStatus(status);
            orderRepository.save(order);

            ra.addFlashAttribute("message", "Order #" + orderId + " status has been updated to " + status);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Quay lại trang chi tiết của chính đơn hàng đó
        return "redirect:/admin/orders/" + orderId;
    }
}
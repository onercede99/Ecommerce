package com.codegym.controller;

import com.codegym.model.Order;
import com.codegym.model.OrderStatus;
import com.codegym.repository.OrderRepository;
import com.codegym.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List; // Vẫn giữ List nếu cần cho các trường hợp khác, nhưng sẽ dùng Page cho listOrders

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private IOrderService orderService;

    private final OrderRepository orderRepository;

    public AdminOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Tạo đối tượng Pageable với sắp xếp theo orderDate giảm dần
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));

        Page<Order> orderPage = orderRepository.findAllWithUserDetails(pageable);

        // Thêm các thuộc tính vào model để truyền sang Thymeleaf
        model.addAttribute("orders", orderPage.getContent()); // Lấy danh sách Order cho trang hiện tại
        model.addAttribute("currentPage", orderPage.getNumber()); // Số trang hiện tại (bắt đầu từ 0)
        model.addAttribute("totalPages", orderPage.getTotalPages()); // Tổng số trang
        model.addAttribute("totalItems", orderPage.getTotalElements()); // Tổng số đơn hàng
        model.addAttribute("pageSize", size); // Kích thước trang hiện tại để dùng trong phân trang

        return "admin/order/list";
    }

    // 2. Hiển thị chi tiết một đơn hàng
    @GetMapping("/{id}")
    public String viewOrderDetail(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            // SỬ DỤNG PHƯƠNG THỨC MỚI TỪ SERVICE để tải chi tiết đơn hàng một cách eager
            Order order = orderService.findByIdWithDetails(id) // Thay đổi từ orderRepository.findById
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
                                    @RequestParam("status") OrderStatus status,
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
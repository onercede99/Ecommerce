package com.codegym.service;

import com.codegym.dto.CartItemDto;
import com.codegym.model.Order;
import com.codegym.model.OrderDetail;
import com.codegym.model.Product;
import com.codegym.model.User;
import com.codegym.repository.OrderRepository;
import com.codegym.repository.ProductRepository;
import com.codegym.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService implements IOrderService{

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ICartService cartService;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, ICartService cartService, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;

    }

    @Transactional
    public Order createOrder(String customerName, String shippingAddress, String phoneNumber,
                             String email, String notes, HttpSession session) {

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setShippingAddress(shippingAddress);
        order.setPhoneNumber(phoneNumber);
        order.setEmail(email);
        order.setNotes(notes);

        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalPrice(cartService.getTotalPrice(session)); // Lấy tổng tiền từ giỏ hàng

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            userRepository.findByUsername(authentication.getName()).ifPresent(order::setUser);
        }

        // 4. Chuyển đổi các item trong giỏ hàng thành OrderDetail
        for (CartItemDto item : cartService.getCart(session).getItems().values()) {
            OrderDetail detail = new OrderDetail();

            // Thiết lập mối quan hệ hai chiều
            detail.setOrder(order);
            order.getOrderDetails().add(detail);

            // Lấy lại Product từ CSDL để đảm bảo dữ liệu là mới nhất và được quản lý bởi JPA
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("Product with ID " + item.getProduct().getId() + " not found while creating order."));

            detail.setProduct(product);
            detail.setPrice(item.getProduct().getPrice()); // Lưu lại giá tại thời điểm mua hàng
            detail.setQuantity(item.getQuantity());
        }

        // 5. Lưu Order vào CSDL.
        // Nhờ CascadeType.ALL, tất cả các OrderDetail trong list cũng sẽ tự động được lưu.
        Order savedOrder = orderRepository.save(order);

        // 6. Xóa giỏ hàng sau khi đã đặt hàng thành công
        cartService.clearCart(session);

        return savedOrder;
    }
    @Override
    public long count() {
        // JpaRepository đã cung cấp sẵn phương thức count()
        return orderRepository.count();
    }

    @Override
    public BigDecimal calculateTotalRevenue() {
        // Gọi phương thức từ repository
        BigDecimal total = orderRepository.findTotalRevenue();

        // XỬ LÝ QUAN TRỌNG NHẤT:
        // Nếu repository trả về null (vì không có đơn hàng nào),
        // chúng ta sẽ trả về BigDecimal.ZERO để tránh lỗi NullPointerException ở Controller.
        if (total == null) {
            return BigDecimal.ZERO;
        }

        return total;
    }
}
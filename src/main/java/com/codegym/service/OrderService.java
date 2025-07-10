package com.codegym.service;

import com.codegym.dto.CartDto;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                             String email, String notes,
                             HttpSession session) { // <-- Chữ ký mới

        // 1. Lấy giỏ hàng từ CartService, không cần truyền session nữa
        CartDto cart = cartService.getCart(session);

        // Kiểm tra nếu giỏ hàng trống thì không cho tạo đơn
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from an empty cart.");
        }

        // 2. Tạo một đối tượng Order mới
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setShippingAddress(shippingAddress);
        order.setPhoneNumber(phoneNumber);
        order.setEmail(email);
        order.setNotes(notes);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalPrice(cart.getTotalPrice());

        // 3. Gán User cho đơn hàng nếu người dùng đã đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            userRepository.findByUsername(authentication.getName()).ifPresent(order::setUser);
        }

        // 4. Chuyển các CartItem thành OrderDetail và thêm vào Order
        for (CartItemDto item : cart.getItems().values()) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);

            // Lấy lại Product từ database để đảm bảo dữ liệu mới nhất
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("Product with ID " + item.getProduct().getId() + " not found while creating order."));

            detail.setProduct(product);
            detail.setPrice(item.getProduct().getPrice()); // Lưu lại giá tại thời điểm mua hàng
            detail.setQuantity(item.getQuantity());

            order.addOrderDetail(detail);
        }

        // 5. Lưu toàn bộ đơn hàng (bao gồm các chi tiết) vào database
        Order savedOrder = orderRepository.save(order);

        // 6. Xóa giỏ hàng sau khi đã tạo đơn thành công
        cartService.clearCart(session);

        // 7. Trả về đơn hàng đã được lưu
        return savedOrder;
    }
    @Override
    public long count() {
        return orderRepository.count();
    }

    @Override
    public BigDecimal calculateTotalRevenue() {
        // Gọi phương thức từ repository
        BigDecimal total = orderRepository.findTotalRevenue();

        if (total == null) {
            return BigDecimal.ZERO;
        }

        return total;
    }
}
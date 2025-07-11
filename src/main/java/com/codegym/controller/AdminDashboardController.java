package com.codegym.controller;

import com.codegym.dto.ChartData;
import com.codegym.dto.DashboardStatsDto;
import com.codegym.repository.ProductRepository;
import com.codegym.repository.UserRepository;
import com.codegym.service.DashboardService;
import com.codegym.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public AdminDashboardController(DashboardService dashboardService, OrderService orderService,
                                    UserRepository userRepository, ProductRepository productRepository) {
        this.dashboardService = dashboardService;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {

        DashboardStatsDto stats = new DashboardStatsDto();


        stats.setTotalUsers(dashboardService.getTotalUsers());
        stats.setTotalProducts(dashboardService.getTotalProducts());
        stats.setTotalOrders(dashboardService.getTotalOrders());
        stats.setTotalRevenue(dashboardService.getTotalRevenue());

        // 3. Thêm ĐỐI TƯỢNG DUY NHẤT 'stats' vào model
        model.addAttribute("stats", stats);

        // ---- KẾT THÚC SỬA ĐỔI ----

        return "admin/dashboard"; // Trả về view
    }

    @GetMapping("/api/chart-data")
    @ResponseBody // Annotation này báo cho Spring trả về JSON, không phải tên view
    public ResponseEntity<Map<String, Object>> getChartData() {
        Map<String, Object> chartData = dashboardService.getChartData();
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/api/daily-revenue-chart-data")
    @ResponseBody // Annotation này báo cho Spring trả về JSON
    public ChartData getDailyRevenueChartData() {
        int numberOfDays = 30; // Bạn có thể tùy chỉnh số ngày ở đây
        return dashboardService.getDailyRevenueChartData(numberOfDays);
    }

    @GetMapping("/home")
    public String adminHome(Authentication authentication) {
        // Lấy thông tin xác thực của người dùng đang đăng nhập
        if (authentication == null) {
            // Nếu vì lý do nào đó không có thông tin xác thực, chuyển về trang login
            return "redirect:/login";
        }

        // Lấy danh sách quyền
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Nếu có quyền ADMIN, chuyển đến dashboard
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }

        // Nếu có quyền STAFF (mà không phải ADMIN), chuyển đến trang quản lý sản phẩm
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
            return "redirect:/admin/products";
        }

        // Mặc định, nếu không khớp quyền nào (dù hiếm), từ chối truy cập
        return "redirect:/access-denied"; // Hoặc có thể là trang lỗi 403
    }
}
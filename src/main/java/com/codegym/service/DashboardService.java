package com.codegym.service;

import com.codegym.dto.MonthlyRevenue;
import com.codegym.repository.OrderRepository;
import com.codegym.repository.ProductRepository;
import com.codegym.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService implements IDashboardService{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DashboardService(UserRepository userRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.findTotalRevenue();

        // Xử lý trường hợp chưa có doanh thu (totalRevenue có thể là null)
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        stats.put("totalUsers", totalUsers);
        stats.put("totalProducts", totalProducts);
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }

    public Map<String, Object> getChartData() {
        List<MonthlyRevenue> monthlyRevenues = orderRepository.findMonthlyRevenue();

        // Chuẩn bị dữ liệu cho Chart.js
        // Chart.js cần 2 mảng: một cho nhãn (labels) và một cho dữ liệu (data)
        String[] labels = new String[12];
        BigDecimal[] data = new BigDecimal[12];

        // Khởi tạo giá trị 0 cho tất cả các tháng
        for (int i = 0; i < 12; i++) {
            labels[i] = "Tháng " + (i + 1);
            data[i] = BigDecimal.ZERO;
        }

        // Điền dữ liệu doanh thu từ CSDL vào đúng tháng
        for (MonthlyRevenue revenue : monthlyRevenues) {
            // revenue.getMonth() trả về số từ 1-12, mảng bắt đầu từ 0
            data[revenue.getMonth() - 1] = revenue.getRevenue();
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", data);

        return chartData;
    }

    @Override
    public List<Map<String, Object>> getMonthlyRevenueForCurrentYear() {

        String sql = "SELECT " +
                "  MONTH(order_date) AS month, " +
                "  SUM(total_price) AS revenue " +
                "FROM " +
                "  orders " +
                "WHERE " +
                "  YEAR(order_date) = YEAR(CURDATE()) AND status = 'DELIVERED' " +
                "GROUP BY " +
                "  MONTH(order_date) " +
                "ORDER BY " +
                "  month;";

        return jdbcTemplate.queryForList(sql);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {

        BigDecimal total = orderRepository.findTotalRevenueOfDeliveredOrders(); // Ví dụ

        return (total == null) ? BigDecimal.ZERO : total;
    }
}
package com.codegym.repository;

import com.codegym.dto.MonthlyRevenue;
import com.codegym.model.Order;
import com.codegym.model.OrderStatus;
import com.codegym.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal findTotalRevenue();

    @Query("SELECT new com.codegym.dto.MonthlyRevenue(MONTH(o.orderDate), SUM(o.totalPrice)) " +
            "FROM Order o " +
            "WHERE o.status = 'DELIVERED' AND YEAR(o.orderDate) = YEAR(CURRENT_DATE) " +
            "GROUP BY MONTH(o.orderDate) " +
            "ORDER BY MONTH(o.orderDate)")
    List<MonthlyRevenue> findMonthlyRevenue();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user")
    List<Order> findAllWithUserDetails(Sort sort);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal findTotalRevenueOfDeliveredOrders();

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithDetails(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Query(value = "SELECT DATE(o.order_date) as orderDay, SUM(o.total_price) as dailyRevenue " +
            "FROM orders o " + // Aliasing the table 'orders' as 'o' for consistency
            "WHERE o.status = :statusDelivered " +
            "AND DATE(o.order_date) >= :startDate AND DATE(o.order_date) <= :endDate " +
            "GROUP BY DATE(o.order_date) " +
            "ORDER BY DATE(o.order_date) ASC",
            nativeQuery = true)
    List<Object[]> findDailyRevenueBetweenDates(
            @Param("statusDelivered") String statusDelivered,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderDetails WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

}
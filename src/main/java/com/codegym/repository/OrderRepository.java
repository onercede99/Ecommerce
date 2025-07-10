package com.codegym.repository;

import com.codegym.dto.MonthlyRevenue;
import com.codegym.model.Order;
import com.codegym.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

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

}
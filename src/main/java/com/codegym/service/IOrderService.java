package com.codegym.service;

import com.codegym.model.Order;
import com.codegym.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IOrderService {
    long count();
    BigDecimal calculateTotalRevenue();
    List<Order> findByUser(User user);
    Optional<Order> findOrderByIdForUser(Long orderId, User user);
    Optional<Order> findByIdWithDetails(Long id);


}

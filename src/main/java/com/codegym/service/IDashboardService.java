package com.codegym.service;

import com.codegym.dto.ChartData;

import java.util.List;
import java.util.Map;

public interface IDashboardService {
    List<Map<String, Object>> getMonthlyRevenueForCurrentYear();
    ChartData getDailyRevenueChartData(int numberOfDays);

    long getTotalUsers();
    long getTotalProducts();
    long getTotalOrders();
    java.math.BigDecimal getTotalRevenue();
    Map<String, Object> getChartData();
}

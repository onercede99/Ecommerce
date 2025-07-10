package com.codegym.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStatsDto {
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
}

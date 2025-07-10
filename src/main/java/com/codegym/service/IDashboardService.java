package com.codegym.service;

import java.util.List;
import java.util.Map;

public interface IDashboardService {
    List<Map<String, Object>> getMonthlyRevenueForCurrentYear();
}

package com.codegym.controller;

import com.codegym.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Map<String, Object> stats = dashboardService.getDashboardStatistics();
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    @GetMapping("/api/chart-data")
    @ResponseBody // Annotation này báo cho Spring trả về JSON, không phải tên view
    public ResponseEntity<Map<String, Object>> getChartData() {
        Map<String, Object> chartData = dashboardService.getChartData();
        return ResponseEntity.ok(chartData); // Trả về dữ liệu với status 200 OK
    }
}
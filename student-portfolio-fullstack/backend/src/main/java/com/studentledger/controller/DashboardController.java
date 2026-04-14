package com.studentledger.controller;

import com.studentledger.dto.ApiResponse;
import com.studentledger.dto.DashboardSummary;
import com.studentledger.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummary> getSummary() {
        return ApiResponse.success("Dashboard summary loaded successfully.", dashboardService.buildSummary());
    }
}

package com.course.securityagent.controller;

import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.dto.DashboardStats;
import com.course.securityagent.service.DashboardService;
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

    @GetMapping("/stats")
    public ApiResponse<DashboardStats> stats() {
        return ApiResponse.ok(dashboardService.stats());
    }
}

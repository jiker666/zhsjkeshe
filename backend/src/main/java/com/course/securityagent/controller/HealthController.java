package com.course.securityagent.controller;

import com.course.securityagent.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

    @Value("${server.port}")
    private String serverPort;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("port", serverPort);
        data.put("database", checkDatabase());
        data.put("time", LocalDateTime.now().toString());
        data.put("version", "final-1.0.0");
        return ApiResponse.ok(data);
    }

    private String checkDatabase() {
        try {
            Integer value = jdbcTemplate.queryForObject("select 1", Integer.class);
            return value != null && value == 1 ? "UP" : "UNKNOWN";
        } catch (Exception exception) {
            return "DOWN: " + exception.getMessage();
        }
    }
}

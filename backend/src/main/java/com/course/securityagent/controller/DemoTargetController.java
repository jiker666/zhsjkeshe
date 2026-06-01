package com.course.securityagent.controller;

import com.course.securityagent.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 课程设计内置演示靶场：仅返回模拟数据，用于展示安全测试流程，不代表真实攻击能力。
 */
@RestController
@RequestMapping("/api/demo-target")
public class DemoTargetController {

    @GetMapping("/public/info")
    public ApiResponse<Map<String, Object>> publicInfo() {
        return ApiResponse.ok(Map.of(
                "name", "Demo Target",
                "scope", "课程设计本地演示目标",
                "safe", true,
                "leakedFlag", "flag{public_info_leak}",
                "note", "该字段仅用于趣味靶场信息泄露题目演示"
        ));
    }

    @GetMapping("/admin/users")
    public ApiResponse<List<Map<String, Object>>> adminUsers() {
        return ApiResponse.ok(List.of(
                Map.of("id", 1, "username", "admin", "role", "ADMIN", "phone", "13800000001"),
                Map.of("id", 2, "username", "demo_user", "role", "USER", "phone", "13800000002")
        ));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<Map<String, Object>> order(@PathVariable Long orderId, @RequestParam(required = false) Long userId) {
        String marker = orderId == 1002 ? "flag{idor_order_1002}" : "demo-order-" + orderId;
        return ApiResponse.ok(Map.of("orderId", orderId, "ownerUserId", userId == null ? 2 : userId, "amount", 399.00, "status", "PAID", "orderMarker", marker));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestParam(defaultValue = "demo") String username,
                                                  @RequestParam(defaultValue = "123456") String password) {
        boolean weak = "admin".equals(username) && ("admin123".equals(password) || "123456".equals(password));
        return ApiResponse.ok(Map.of("username", username, "token", "demo-token-" + password.length(), "weakPasswordHit", weak, "weakPasswordFlag", weak ? "flag{weak_password_admin123}" : "-"));
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile(@RequestParam(defaultValue = "1") Long userId) {
        return ApiResponse.ok(Map.of("userId", userId, "email", "user" + userId + "@demo.local", "phone", "1380000" + userId, "debugToken", "debug-token-demo", "tokenRiskFlag", "flag{token_exposure_risk}"));
    }

    @PostMapping("/update-role")
    public ApiResponse<Map<String, Object>> updateRole(@RequestParam Long userId, @RequestParam String role) {
        return ApiResponse.ok(Map.of("userId", userId, "acceptedRole", role, "tamperFlag", "flag{role_should_not_be_trusted}", "note", "演示接口模拟参数篡改风险，不执行真实权限变更"));
    }
}

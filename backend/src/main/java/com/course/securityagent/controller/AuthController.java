package com.course.securityagent.controller;

import com.course.securityagent.common.ApiResponse;
import com.course.securityagent.dto.LoginRequest;
import com.course.securityagent.dto.LoginResponse;
import com.course.securityagent.dto.RegisterRequest;
import com.course.securityagent.dto.UserDTO;
import com.course.securityagent.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("ok");
    }
}

package com.course.securityagent.service;

import com.course.securityagent.dto.LoginRequest;
import com.course.securityagent.dto.LoginResponse;
import com.course.securityagent.dto.RegisterRequest;
import com.course.securityagent.dto.UserDTO;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    UserDTO register(RegisterRequest request);
}

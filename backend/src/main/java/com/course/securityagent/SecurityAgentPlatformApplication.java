package com.course.securityagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.course.securityagent.mapper")
@SpringBootApplication
public class SecurityAgentPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityAgentPlatformApplication.class, args);
    }
}

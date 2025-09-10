package com.example.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException; // Bắt lỗi cụ thể này
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component // Đánh dấu là một Spring Bean
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper(); // Dùng để tạo JSON response

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        logger.warn("Authentication failed: {}", exception.getMessage()); // Log lỗi gốc

        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // Mã lỗi 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8"); // Đảm bảo hỗ trợ tiếng Việt

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis());
        data.put("status", HttpStatus.UNAUTHORIZED.value());
        data.put("error", "Unauthorized"); // Có thể giữ nguyên hoặc đổi

        if (exception instanceof BadCredentialsException) {
            data.put("message", "Tài khoản hoặc mật khẩu không chính xác. Vui lòng thử lại.");
        } else {
            // Các loại lỗi AuthenticationException khác (tài khoản bị khóa, hết hạn,...)
            data.put("message", "Lỗi xác thực: " + exception.getMessage()); // Hoặc thông báo chung chung hơn
        }

        data.put("path", request.getRequestURI());

        response.getOutputStream().println(objectMapper.writeValueAsString(data));
    }
}

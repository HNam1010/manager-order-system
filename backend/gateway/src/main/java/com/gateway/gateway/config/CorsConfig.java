package com.gateway.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils; // Import reactive
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter; // Import reactive
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono; // Import reactive
import java.util.List; // Import List

@Configuration
public class CorsConfig { // Đặt tên class phù hợp

    // Lấy danh sách các origin được phép từ application.properties
    // private static final String ALLOWED_ORIGINS = "http://localhost:4200,http://localhost:4300";
    private static final List<String> ALLOWED_ORIGINS = List.of("http://localhost:4200", "http://localhost:4300");
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS";
    private static final String ALLOWED_HEADERS = "Origin, Accept, Content-Type, Authorization, X-Guest-ID, X-User-ID, X-User-Roles"; // Thêm các header tùy chỉnh bạn dùng
    private static final String MAX_AGE = "3600"; // Thời gian cache preflight request (giây)

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();

            if (CorsUtils.isCorsRequest(request)) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                String origin = request.getHeaders().getOrigin();

                // Add this logging:
                System.out.println("CORS Filter - Processing request from origin: " + origin);

                if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
                    // Add this logging:
                    System.out.println("CORS Filter - Allowing origin: " + origin);

                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
                    headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);

                    // Add this logging:
                    System.out.println("CORS Filter - Setting ALLOWED_HEADERS: " + ALLOWED_HEADERS);

                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                } else {
                    System.out.println("CORS Filter - Origin not allowed: " + origin);
                }

                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            return chain.filter(ctx);
        };
    }
}

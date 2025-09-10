package com.gateway.gateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    @Autowired
    private JwtUtils jwtUtils; // Inject JwtUtils

    private final List<String> alwaysPublicPaths = List.of(
            "/api/v1/auth/",           // Login, Register
            "/api/v1/products/images/" // Lấy ảnh công khai
    );

    private final List<String> publicGetPaths = List.of(
            "/api/v1/products",        // Xem danh sách sản phẩm
            "/api/v1/product-types",   // Xem danh sách loại sản phẩm
            "/api/v1/payment-info",   // Xem thông tin thanh toán (cho phép cả /bank-transfer)
            "/api/v1/products/"         // Nếu muốn bao gồm cả /products/{id}
    );

    // THÊM Danh sách đường dẫn GET Order chi tiết cho khách
    private final String guestOrderDetailPathPrefix = "/api/v1/orders/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        log.debug("AuthFilter processing request: {} {}", method, path);

        // 1. KIỂM TRA CÁC PATH LUÔN PUBLIC
        boolean isAlwaysPublic = alwaysPublicPaths.stream()
                .anyMatch(p -> path.startsWith(p));
        if (isAlwaysPublic) {
            log.debug("Path {} is always public, allowing request.", path);
            return chain.filter(exchange);
        }

        // 2. KIỂM TRA CÁC PATH PUBLIC CHỈ CHO PHÉP GET
        boolean isPublicGet = (method == HttpMethod.GET) &&
                publicGetPaths.stream()
                        // Kiểm tra chính xác path hoặc bắt đầu bằng path + "/"
                        .anyMatch(p -> path.equals(p) || path.startsWith(p + "/"));
        if (isPublicGet) {
            log.debug("Path {} (Method: GET) is conditionally public (in publicGetPaths). Allowing request without token.", path);
            return chain.filter(exchange); // <-- Cho phép đi tiếp KHÔNG CẦN TOKEN
        }

        // 3. XỬ LÝ CÁC PATH CÒN LẠI
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String jwt = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // 4. XỬ LÝ RIÊNG CHO API KHÔNG CẦN TOKEN (Guest)
        if (jwt == null) {
            if (path.startsWith("/api/v1/cart")) { // Cart API của khách
                log.debug("No JWT for Cart API path: {}. Allowing guest.", path);
                return chain.filter(exchange);
            } else if (path.equals("/api/v1/orders") && method == HttpMethod.POST) { // Đặt hàng của khách
                log.debug("No JWT for Order POST path: {}. Allowing guest.", path);
                return chain.filter(exchange);
            } else if (path.startsWith(guestOrderDetailPathPrefix) && method == HttpMethod.GET) { // Xem chi tiết đơn hàng khách
                log.debug("No JWT for GET Order Detail path: {}. Allowing guest (backend validates token).", path);
                return chain.filter(exchange);
            }
            // --- Nếu đến đây mà jwt vẫn là null, nghĩa là không khớp các trường hợp trên -> Lỗi 401 ---
            log.warn("Missing Authorization header for protected path: {} {}", method, path);
            return this.onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
        }


        // 5. TỪ ĐÂY TRỞ ĐI: YÊU CẦU PHẢI CÓ TOKEN HỢP LỆ vì đã kiểm tra jwt != null
        // 6. Validate token
        if (!jwtUtils.validateJwtToken(jwt)) {
            log.warn("Invalid JWT token for path: {} {}", method, path);
            return this.onError(exchange, "Invalid JWT Token", HttpStatus.UNAUTHORIZED);
        }

        // 7. Token hợp lệ -> Trích xuất thông tin và thêm header
        try {
            log.info("Headers trước khi có X-User-ID và X-User-Roles: {}", request.getHeaders().toString());
            log.info("JWT Token: {}", jwt); // Log token để kiểm tra
            Long userId = jwtUtils.getUserIdFromJwtToken(jwt);
            List<String> roles = jwtUtils.getRolesFromJwtToken(jwt);
            log.info("Extracted userId: {}", userId); // Log userId trích xuất được
            log.info("Extracted roles: {}", roles); // Log roles trích xuất được

            if (userId == null || roles == null || roles.isEmpty()) { // Vẫn kiểm tra roles không rỗng
                log.error("Could not extract valid userId or non-empty roles from token for path: {} {}", method, path);
                return this.onError(exchange, "Invalid token claims (userId or roles missing/empty)", HttpStatus.UNAUTHORIZED);
            }

            log.debug("Token validated for user ID: {} with roles: {} for path: {} {}", userId, roles, method, path);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-ID", String.valueOf(userId)) // Gửi ID dạng String
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            log.info("Mutated Headers (đã thêm X-User-ID, X-User-Roles): {}", mutatedRequest.getHeaders().toString()); // Log headers sau khi thêm

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Error processing JWT token for path: {} {}. Error: {}", method, path, e.getMessage(), e);
            return this.onError(exchange, "Error processing token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hàm helper xử lý trả về lỗi (giữ nguyên)
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.warn("AuthFilter returning error - Status: {}, Message: {}", httpStatus, err);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Chạy gần như đầu tiên
    }
}
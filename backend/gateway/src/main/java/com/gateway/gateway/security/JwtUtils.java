package com.gateway.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }


    // Lấy username từ token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Validate token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        }
        return false;
    }

    /*
      Helper: Lấy token từ Header Authorization của ServerHttpRequest (WebFlux).
      @param request Đối tượng ServerHttpRequest.
      @return Chuỗi JWT hoặc null nếu không tìm thấy.
     */
    public String parseJwt(ServerHttpRequest request) {
        // Sử dụng request.getHeaders() trả về HttpHeaders
        String headerAuth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Bỏ "Bearer "
        }
        return null;
    }

    public Long getUserIdFromJwtToken(String token) {
        // thêm claim "userId" khi tạo token
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        // Lấy claim 'userId' và chuyển đổi sang Long
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Integer) { // JWT có thể trả về Integer
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                logger.error("Cannot parse userId claim to Long: {}", userIdObj);
                return null; // Hoặc ném exception
            }
        }
        logger.error("UserId claim is missing or has unexpected type: {}", userIdObj);
        return null; // Hoặc ném exception
    }

    @SuppressWarnings("unchecked") // Bỏ cảnh báo unchecked cast khi lấy List từ claim
    public List<String> getRolesFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                    .parseClaimsJws(token).getBody();

            // Lấy claim 'roles'
            Object rolesObj = claims.get("roles"); // Tên claim phải khớp với lúc tạo token

            if (rolesObj instanceof List) {
                // Kiểm tra xem các phần tử trong List có phải là String không (để an toàn)
                List<?> rawList = (List<?>) rolesObj;
                if (!rawList.isEmpty() && rawList.get(0) instanceof String) {
                    // Nếu đúng là List<String>, ép kiểu và trả về
                    return (List<String>) rawList;
                } else if (rawList.isEmpty()) {
                    return Collections.emptyList(); // Trả về list rỗng nếu claim roles là list rỗng
                } else {
                    logger.warn("Roles claim contains non-String elements.");
                    return Collections.emptyList(); // Hoặc xử lý khác nếu cần
                }
            } else {
                logger.warn("Roles claim is missing or not a List in the JWT token.");
                return Collections.emptyList(); // Trả về list rỗng nếu claim không tồn tại hoặc sai kiểu
            }
        } catch (Exception e) {
            logger.error("Error parsing roles from JWT token: {}", e.getMessage());
            return Collections.emptyList(); // Trả về list rỗng khi có lỗi parsing
        }
    }


}
package com.example.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Skip filter for /api/v1/auth/** endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/v1/auth/")) {
            logger.debug("Skipping AuthTokenFilter for request URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String jwt = parseJwt(request);
            logger.debug("AuthTokenFilter - JWT from request: {}", jwt); // Log JWT

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                logger.debug("AuthTokenFilter - JWT is valid");
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.debug("AuthTokenFilter - Username from JWT: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("AuthTokenFilter - UserDetails loaded: {}", userDetails); // Log UserDetails

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                logger.debug("AuthTokenFilter - Authentication object created: {}", authentication); // Log Authentication

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                logger.debug("AuthTokenFilter - Setting Authentication in SecurityContext...");
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("AuthTokenFilter - Authentication set successfully in SecurityContext");

            } else {
                logger.warn("AuthTokenFilter - JWT is null or invalid");
            }
        } catch (Exception e) {
            logger.error("AuthTokenFilter - Cannot set user authentication", e); // Log lỗi chi tiết hơn
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
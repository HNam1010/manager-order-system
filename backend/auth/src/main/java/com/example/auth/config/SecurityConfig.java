package com.example.auth.config;

import com.example.auth.security.AuthEntryPointJwt;
import com.example.auth.security.AuthTokenFilter;
import com.example.auth.security.UserDetailsServiceImpl; // Import service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity // Bật cấu hình bảo mật web của Spring Security
@EnableMethodSecurity(prePostEnabled = true) // Bật bảo mật ở mức phương thức (vd: @PreAuthorize)
public class SecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService; // Service để load user

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    // Tạo Bean cho AuthTokenFilter
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    // Cấu hình AuthenticationProvider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Cung cấp UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());   // Cung cấp PasswordEncoder
        return authProvider;
    }

    // Tạo Bean cho AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Tạo Bean cho PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/v1/auth/**").permitAll() // Login/Register public
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated() // User lấy thông tin của chính mình
                                .requestMatchers(HttpMethod.PUT, "/api/v1/users/me").authenticated() // User cập nhật profile của chính mình
                                .requestMatchers("/api/v1/users/**").hasRole("ADMIN") // Các API user khác (/api/v1/users, /api/v1/users/{id}) chỉ cho Admin
                                .requestMatchers("/api/v1/roles/**").hasRole("ADMIN") // API role cho Admin (hoặc authenticated() nếu user thường cũng được xem?)
                                .anyRequest().authenticated()
                );



        // --- CẤU HÌNH CORS ---
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
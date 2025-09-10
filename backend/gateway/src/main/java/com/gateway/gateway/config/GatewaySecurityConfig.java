package com.gateway.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // Dùng cho WebFlux (Gateway)
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Tắt CSRF
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/**").permitAll() // Cho phép tất cả để AuthFilter xử lý
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Tắt Basic Auth
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable); // Tắt Form Login

        return http.build();
    }
}
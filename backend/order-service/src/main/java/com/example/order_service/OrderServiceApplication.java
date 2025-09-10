package com.example.order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.ComponentScan; // <-- THÊM IMPORT NÀY

@SpringBootApplication
@EnableDiscoveryClient // Bật Eureka Client
@EnableFeignClients // <-- Bật Feign Client
@EnableAsync  // gửi mail
@ComponentScan(basePackages = {
		"com.example.order_service",          // Package gốc của service này
		"com.example.be.commons.handler"      // Package cha chứa handler (hoặc chỉ định cụ thể hơn nếu muốn)
		// Nếu GlobalExceptionHandler nằm trực tiếp trong com.example.be.commons thì dùng "com.example.be.commons"
		// Hoặc chỉ định chính xác: "com.example.be.commons.handler.controllerad"
})
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}

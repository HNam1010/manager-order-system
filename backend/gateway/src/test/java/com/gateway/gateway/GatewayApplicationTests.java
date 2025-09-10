package com.gateway.gateway; // Hoặc package test của bạn

import org.junit.jupiter.api.Test; // Import đúng
import org.springframework.boot.test.context.SpringBootTest; // Import đúng

@SpringBootTest // Annotation để chạy test Spring Boot
class GatewayApplicationTests {

	@Test // Annotation đánh dấu phương thức test
	void contextLoads() {
		// Test này sẽ pass nếu ứng dụng khởi động thành công context
	}

}
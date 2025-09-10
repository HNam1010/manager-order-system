package com.example.cart_service.service.servicerepo;

import com.example.be.commons.ApiResponse;
import com.example.cart_service.dto.reponse.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List; // Nếu cần lấy nhiều sản phẩm

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/v1/products")
public interface ProductServiceClient {

    //Product Service trả về ApiResponse<ProductDTO> chỉ ProductDTO nếu Product Service không dùng ApiResponse cho getById
    @GetMapping("/{id}")
    ApiResponse<ProductDTO> getProductById(@PathVariable("id") Long productId);

    // Có thể thêm các phương thức khác nếu cần, ví dụ lấy nhiều sản phẩm
    @PostMapping("/batch")
    ApiResponse<List<ProductDTO>> getProductsByIds(@RequestBody List<Long> productIds);
}
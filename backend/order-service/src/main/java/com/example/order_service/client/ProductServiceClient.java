package com.example.order_service.client;

import com.example.be.commons.ApiResponse;
import com.example.order_service.dto.client.ProductData;
import com.example.order_service.dto.request.UpdateStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "${product.service.name:product-service}", path = "/api/v1/products") // Lấy tên từ properties
public interface ProductServiceClient {
    @GetMapping("/{id}")
    ApiResponse<ProductData> getProductById(@PathVariable("id") Long productId);

    // API MỚI: Giảm tồn kho hàng loạt (dùng PATCH hoặc PUT)
    @PutMapping("/stock/decrease")      // Thành dòng này
    ApiResponse<Void> decreaseStock(@RequestBody List<UpdateStockRequest> updateRequests);
}
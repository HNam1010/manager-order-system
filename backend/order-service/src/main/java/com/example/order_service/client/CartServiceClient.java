package com.example.order_service.client;

import com.example.be.commons.ApiResponse;
import com.example.order_service.dto.client.CartItemData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


@FeignClient(name = "${cart.service.name:cart-service}", path = "/api/v1/cart") // Lấy tên từ properties
public interface CartServiceClient {
    // Lấy giỏ hàng (truyền header userId hoặc guestId do OrderService biết)
    @GetMapping
    ApiResponse<List<CartItemData>> getCartItems(
            @RequestHeader(name = "X-User-ID", required = false) Long userId,
            @RequestHeader(name = "X-Guest-ID", required = false) String guestId
    );

    // Xóa giỏ hàng (truyền header userId hoặc guestId)
    @DeleteMapping
    // Hoặc có thể là DELETE /items
    ApiResponse<Void> clearCart(
            @RequestHeader(name = "X-User-ID", required = false) Long userId,
            @RequestHeader(name = "X-Guest-ID", required = false) String guestId
    );
}
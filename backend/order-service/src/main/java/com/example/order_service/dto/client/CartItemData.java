package com.example.order_service.dto.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItemData {
    private Long productId;
    private Integer quantity;

    // Không cần các trường khác như cartItemId, tên sp, giá sp,...
    // vì OrderService sẽ tự lấy thông tin mới nhất từ ProductService
}
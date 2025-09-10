package com.example.order_service.dto.reponse;


import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant; // Sử dụng Instant cho nhất quán

@Data
public class OrderItemResponse {
    private Long orderDetailId; // ID của bản ghi order_details
    private Long productId;     // ID gốc của sản phẩm
    private String productName; // Snapshot
    private String productBrand; // Snapshot (Optional)
    private String productImagePath; // Snapshot (Optional, đường dẫn tương đối)
    private BigDecimal productPrice; // Snapshot giá lúc mua
    private Integer quantity;
    private BigDecimal totalPrice; // quantity * productPrice
}

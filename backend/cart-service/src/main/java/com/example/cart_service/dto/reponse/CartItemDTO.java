package com.example.cart_service.dto.reponse;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private Long productId;
    private Integer quantity;

    //Thông tin sản phẩm lấy từ Product Service
    private String productName;
    private String productBrand;
    private BigDecimal productPrice;
    private String productImageUrl; // Đường dẫn tương đối
    private Long productStock; // Số lượng tồn kho (để kiểm tra lại ở frontend nếu cần)

    private BigDecimal totalPrice; // Tính toán: productPrice * quantity

}

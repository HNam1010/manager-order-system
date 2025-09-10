package com.example.cart_service.dto.reponse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant; // Giả sử ProductResponse dùng Instant

@Data
@NoArgsConstructor
public class ProductDTO {
    private Long serialId;
    private String name;
    private String brand;
    private String imagePath; // Đường dẫn tương đối
    private BigDecimal price;
    private Long quantity; // Số lượng tồn kho

}

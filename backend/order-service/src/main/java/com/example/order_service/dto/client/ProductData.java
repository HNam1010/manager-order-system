package com.example.order_service.dto.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductData {
    private Long serialId;
    private String name;
    private String brand;
    private BigDecimal price;
    private Long quantity; // Số lượng tồn kho
    private String imagePath;
}
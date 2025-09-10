package com.example.product_service.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductResponse {
    private Long serialId;
    private String name;
    private String brand;
    private String imagePath; // Giữ lại đường dẫn tương đối
    private BigDecimal price;
    private Instant updateDate; // Dùng Instant khớp Entity
    private Long quantity;
    private String description;
    private String size;
    private String productTypeName; // Mapper sẽ lấy từ ProductType liên kết
    private Integer productTypeId;  // Mapper sẽ lấy từ ProductType liên kết
    private Long userId;            // Mapper sẽ lấy trực tiếp từ Product entity
    private Instant createdDate;  // Dùng Instant khớp Entity

}
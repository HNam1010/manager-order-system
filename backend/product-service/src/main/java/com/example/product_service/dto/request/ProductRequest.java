package com.example.product_service.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
public class ProductRequest {
    @NotBlank String name;
    String brand;
    String description;
    @NotNull @DecimalMin("0.0") BigDecimal price;
    @NotNull @Min(0) Long quantity;
    @NotNull Integer productTypeId; // Khớp kiểu khóa chính ProductType

    @Size(max = 50, message = "Kích thước không được vượt quá 50 ký tự") // Ví dụ validation
    String size;
    Long userId;

}
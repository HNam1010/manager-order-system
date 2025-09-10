package com.example.product_service.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
public class ProductTypeRequest {
    @NotBlank
    private String name;

    @NotNull
    private Long sumQuantity;

    @NotNull
    private BigDecimal price;

}

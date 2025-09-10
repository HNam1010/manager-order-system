package com.example.product_service.dto.response;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class ProductTypeResponse {
    private Integer serialId;
    private String name;
    private Long sumQuantity;
    private BigDecimal price;

}
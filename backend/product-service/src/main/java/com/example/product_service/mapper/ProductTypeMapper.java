package com.example.product_service.mapper;

import com.example.product_service.dto.response.ProductTypeResponse;
import com.example.product_service.entity.ProductType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") // Đảm bảo có componentModel
public interface ProductTypeMapper {
    ProductTypeResponse toResponse(ProductType productType);
}
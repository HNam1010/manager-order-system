package com.example.product_service.mapper;


import com.example.product_service.entity.Product;
import com.example.product_service.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper { // Sửa thành interface

    @Mapping(source = "serialId", target = "serialId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "brand", target = "brand")
    @Mapping(source = "imagePath", target = "imagePath")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "updatedAt", target = "updateDate") // Khớp tên thuộc tính entity
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "size", target = "size")
    @Mapping(source = "productType.serialId", target = "productTypeId")
    @Mapping(source = "productType.name", target = "productTypeName")

    @Mapping(source = "userId", target = "userId") // Map trực tiếp từ userId của Product entity

    @Mapping(source = "createdAt", target = "createdDate") // Khớp tên thuộc tính entity
    ProductResponse toResponse(Product product); // Sửa phương thức abstract

    List<ProductResponse> toResponseList(List<Product> products); // Sửa phương thức abstract
}

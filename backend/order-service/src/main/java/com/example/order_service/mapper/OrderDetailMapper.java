package com.example.order_service.mapper;


import com.example.order_service.dto.reponse.OrderItemResponse;
import com.example.order_service.entity.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {

    // Map từ OrderDetail entity sang OrderItemResponse DTO
    @Mapping(source = "orderDetailId", target = "orderDetailId") // Map ID của chính nó
    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "productBrand", target = "productBrand")
    @Mapping(source = "productImagePath", target = "productImagePath") // Thêm nếu có
    @Mapping(source = "productPrice", target = "productPrice")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "totalPrice", target = "totalPrice")
    OrderItemResponse toOrderItemResponse(OrderDetail orderDetail);
}
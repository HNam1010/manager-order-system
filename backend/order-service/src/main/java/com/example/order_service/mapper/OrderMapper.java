package com.example.order_service.mapper;

import com.example.order_service.dto.reponse.OrderResponse;
import com.example.order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List; // Import List

// Khai báo uses = OrderDetailMapper để MapStruct biết cách map List<OrderDetail> sang List<OrderItemResponse>
@Mapper(componentModel = "spring", uses = {OrderDetailMapper.class})
public interface OrderMapper {

    @Mapping(source = "serialId", target = "serialId")
    @Mapping(source = "orderCode", target = "orderCode")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "customerName", target = "customerName")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "shippingAddress", target = "shippingAddress")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "orderNotes", target = "orderNotes")
    @Mapping(source = "totalAmount", target = "totalAmount")
    @Mapping(source = "status.serialId", target = "statusId")         // Map từ đối tượng Status
    @Mapping(source = "status.statusCode", target = "statusCode")     // Map từ đối tượng Status
    @Mapping(source = "status.description", target = "statusDescription") // Map từ đối tượng Status
    @Mapping(source = "paymentMethod", target = "paymentMethod")
    @Mapping(source = "orderDate", target = "orderDate")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "orderDetails", target = "items") // Map List<OrderDetail> sang List<OrderItemResponse>
    @Mapping(source = "guestToken", target = "guestToken") // lấy guest khi k đăng nhập
    OrderResponse toOrderResponse(Order order);

    List<OrderResponse> toOrderResponseList(List<Order> orders); // Để map danh sách nếu cần
}

package com.example.order_service.service.servicerepo;


import com.example.order_service.dto.request.PlaceOrderRequest;
import com.example.order_service.dto.reponse.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse placeOrder(Long userId, String guestId, PlaceOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    OrderResponse getOrderByIdAndUser(Long orderId, Long userId); // Cho user xem chi tiết đơn của mình

    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable); // Lịch sử đơn hàng user

    Page<OrderResponse> getAllOrders(Pageable pageable, Integer statusId); // Lấy tất cả cho Admin

    OrderResponse updateOrderStatus(Long orderId, Integer newStatusId); // Cập nhật trạng thái

    OrderResponse getGuestOrderByIdAndToken(Long orderId, String guestToken); // <-- Thêm hàm này


}
package com.example.order_service.dto.reponse;


import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant; // Sử dụng Instant
import java.util.List;

@Data // Hoặc Getters/Setters
public class OrderResponse {
    private Long serialId;        // ID của Order
    private String orderCode;      // Mã đơn hàng (nếu có)
    private Long userId;          // ID người dùng (nếu có)
    private String customerName;
    private String phoneNumber;
    private String shippingAddress;
    private String email;
    private String orderNotes;
    private BigDecimal totalAmount;    // Tổng tiền cuối cùng
    private Integer statusId;       // ID trạng thái
    private String statusCode;     // Mã trạng thái (ví dụ: "CONFIRMED")
    private String statusDescription; // Mô tả trạng thái
    private String paymentMethod;
    private Instant orderDate;      // Ngày đặt hàng
    private Instant updatedAt;      // Ngày cập nhật cuối
    private List<OrderItemResponse> items; // Danh sách chi tiết sản phẩm


    private String guestToken; // Token tạm thời để khách xem chi tiết

}
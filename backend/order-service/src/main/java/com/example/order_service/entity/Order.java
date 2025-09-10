package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders") // Khớp tên bảng trong DB
@Data
@NoArgsConstructor
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serialId;

    @Column(name = "order_code", length = 50, unique = true)
    private String orderCode; // Có thể tạo tự động

    @Column(name = "user_id") // Chỉ lưu ID, không có FK entity
    private Long userId;

    @Column(name = "customer_name", length = 100, nullable = false)
    private String customerName;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "shipping_address", columnDefinition = "TEXT", nullable = false)
    private String shippingAddress;

    @Column(length = 255)
    private String email;

    @Column(name = "order_notes", columnDefinition = "TEXT")
    private String orderNotes;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY) // Nên LAZY, fetch khi cần map DTO
    @JoinColumn(name = "status_id", nullable = false) // FK đến OrderStatus trong cùng DB
    private OrderStatus status;

    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod; // "COD", "BANK_TRANSFER", "VNPAY", ...

    @Column(name = "order_date", nullable = false, updatable = false)
    private Instant orderDate = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    // Quan hệ với OrderDetail (CascadeType.ALL để lưu/xóa detail cùng order)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Helper method để thêm OrderDetail (đảm bảo quan hệ hai chiều)
    public void addOrderDetail(OrderDetail detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
    }

    public void removeOrderDetail(OrderDetail detail) {
        orderDetails.remove(detail);
        detail.setOrder(null);
    }

    // *** THÊM TRƯỜNG NÀY ***
    @Column(name = "guest_token", length = 36, unique = true) // UUID là 36 ký tự
    private String guestToken; // Lưu token tạm thời cho khách

    @Column(name = "guest_token_expires_at") // Optional: Thêm thời gian hết hạn cho token
    private Instant guestTokenExpiresAt;


    // Helper method (tùy chọn)
    private String generateOrderCode() {
        return "OR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "order_details") // Khớp tên bảng trong DB
@Data
@NoArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderDetailId;

    @ManyToOne(fetch = FetchType.LAZY) // Quan hệ ngược lại với Order
    @JoinColumn(name = "order_id", nullable = false) // FK đến Order trong cùng DB
    private Order order;

    @Column(name = "product_id", nullable = false) // Chỉ lưu ID sản phẩm
    private Long productId;

    // --- Snapshot thông tin sản phẩm ---
    @Column(name = "product_name", length = 255, nullable = false)
    private String productName;

    @Column(name = "product_brand", length = 100)
    private String productBrand;

    @Column(name = "product_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal productPrice; // Giá lúc mua

    @Column(name = "product_image_path", length = 255) // Thêm nếu cần lưu path ảnh
    private String productImagePath;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalPrice; // price * quantity
}
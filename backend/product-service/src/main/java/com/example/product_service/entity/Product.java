package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serialId;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "size", length = 50) // Độ dài tùy chỉnh
    private String size; // Ví dụ: "S, M, L" hoặc chỉ "M", "Free Size"

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Long quantity;

    // Quan hệ với ProductType (trong cùng service)
    @ManyToOne(fetch = FetchType.LAZY) // LAZY là tốt nhất
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    // Chỉ lưu ID người dùng, không có quan hệ Entity trực tiếp
    @Column(name = "user_id") // Đảm bảo tên cột khớp DB
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
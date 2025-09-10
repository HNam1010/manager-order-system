package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List; // Vẫn cần List để @OneToMany hoạt động

@Entity
@Data
@NoArgsConstructor
@Table(name = "product_types" )

public class ProductType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serialId;
    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    // Quan hệ ngược lại với Product (vẫn cần để mappedBy hoạt động)
    @OneToMany(mappedBy = "productType", fetch = FetchType.LAZY)
    private List<Product> products;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
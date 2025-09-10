package com.example.cart_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "shopping_carts" ,
        uniqueConstraints = { // Định nghĩa ràng buộc unique ở đây
                @UniqueConstraint(columnNames = {"user_id", "product_id"}, name = "uq_user_product"),
                @UniqueConstraint(columnNames = {"guest_id", "product_id"}, name = "uq_guest_product")
        })

public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId; // Khóa chính

    @Column(name = "user_id")
    private Long userId; // Nullable

    @Column(name = "guest_id")
    private String guestId; // Nullable

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt = Instant.now();
}

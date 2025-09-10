package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_status") // Khớp tên bảng trong DB
@Data
@NoArgsConstructor
public class OrderStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serialId;

    @Column(name = "status_code", length = 50, nullable = false, unique = true)
    private String statusCode; // Ví dụ: PENDING_CONFIRMATION, CONFIRMED

    @Column(columnDefinition = "TEXT")
    private String description;
}

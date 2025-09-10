package com.example.auth.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "roles")
public class Role{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serialId; // Khớp với DB

    @Column(length = 50, nullable = false, unique = true)
    private String name; // Ví dụ: ROLE_ADMIN, ROLE_CUSTOMER

    @Column(length = 20)
    private String priority; // Giữ lại nếu cần

    public Role(Integer serialId, String name, String priority) {
        this.serialId = serialId;
        this.name = name;
        this.priority = priority;
    }

}
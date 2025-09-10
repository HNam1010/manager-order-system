package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serialId;

    @Column(name = "user_name", length = 255, nullable = false, unique = true)
    private String username;

    @Column(name = "pass_word", length = 255, nullable = false)
    private String password;

    @Column(name = "birth_day", length = 50)
    private String birthDay;

    @Column(length = 255)
    private String address;

    @Column(length = 255, unique = true)
    private String email;

    @Column(length = 20, unique = true)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public User(Long serialId, String username, String password, String birthDay, String address, String email, String phone, Role role, Instant createdAt, Instant updatedAt) {
        this.serialId = serialId;
        this.username = username;
        this.password = password;
        this.birthDay = birthDay;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
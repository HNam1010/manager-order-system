package com.example.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Chỉ chứa các trường Admin được phép cập nhật
public class UserUpdateRequest {

    /// Email (optional)
    @Email(message = "Email không hợp lệ")
    private String email;

    // Vai trò (optional)
    private String role;

    // Các thông tin khác (optional)
    private String birthDay;
    private String address;


    public UserUpdateRequest(String email, String role, String birthDay, String address) {
        this.email = email;
        this.role = role;
        this.birthDay = birthDay;
        this.address = address;
    }

}
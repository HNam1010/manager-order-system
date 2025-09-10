package com.example.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileUpdateRequest {
    // Email (optional)
    @Email(message = "Email không hợp lệ")
    private String email;

    // Các thông tin khác (optional)
    private String phone;

    private String address;

    private String birthDay; // chuỗi có dạng "yyyy-MM-dd"

    private String currentPassword;   // Thêm nếu cho đổi mật khẩu
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;       // Thêm nếu cho đổi mật khẩu


    public UserProfileUpdateRequest(String email, String phone, String address, String birthDay, String currentPassword, String newPassword) {
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.birthDay = birthDay;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }


}
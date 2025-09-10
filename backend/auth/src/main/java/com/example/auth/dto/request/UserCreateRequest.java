package com.example.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "Username là bắt buộc")
    @Size(min = 3, max = 50, message = "Username phải có từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    private String email;

    private String birthDay;

    private String address;

    @NotBlank(message = "Vai trò là bắt buộc")
    private String role;


    public UserCreateRequest(String username, String password, String email, String birthDay, String address, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.birthDay = birthDay;
        this.address = address;
        this.role = role;
    }


}

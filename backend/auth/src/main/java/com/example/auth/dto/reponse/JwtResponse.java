package com.example.auth.dto.reponse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer"; // Giá trị mặc định
    private Long id;
    private String username;
    private String email;
    private List<String> roles; // Danh sách tên các roles (ví dụ: ["ROLE_ADMIN", "ROLE_USER"])

    // Constructor để dễ dàng tạo đối tượng response
    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

}

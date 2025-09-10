package com.example.auth.dto.reponse;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleResponse {
    private Integer serialId; // Khớp với kiểu khóa chính của Role entity (Integer)
    private String name; // Tên vai trò (ví dụ: ADMIN, CUSTOMER)


}

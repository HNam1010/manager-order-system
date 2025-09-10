package com.example.auth.dto.reponse;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String phone;      //Thêm vào
    private String address;     //Thêm vào
    private String birthDay;     //Thêm vào

    public UserResponse(Long id, String username, String email, List<String> roles, String phone, String address, String birthDay) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.phone = phone;
        this.address = address;
        this.birthDay = birthDay;
    }

}

package com.example.auth.service.servicerepo;


import com.example.auth.dto.reponse.JwtResponse;
import com.example.auth.dto.request.LoginRequest;
import com.example.auth.dto.request.UserCreateRequest;
import org.springframework.http.ResponseEntity; // Dùng cho login

public interface AuthService {
    ResponseEntity<?> registerUser(UserCreateRequest userCreateRequest);
    ResponseEntity<JwtResponse> authenticateUser(LoginRequest loginRequest);
}
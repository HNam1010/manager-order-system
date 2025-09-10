package com.example.auth.controller;

import com.example.auth.dto.reponse.JwtResponse;
import com.example.auth.dto.request.LoginRequest;
import com.example.auth.dto.request.UserCreateRequest;
import com.example.auth.dto.request.ResetPasswordRequest;
import com.example.auth.security.JwtUtils;
import com.example.auth.security.UserDetailsImpl;
import com.example.auth.service.servicerepo.AuthService;
import com.example.auth.service.servicerepo.UserService;
import com.example.be.commons.ApiResponse;
import com.example.be.commons.handler.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService; // Inject UserService

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils jwtUtils;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Trả về JwtResponse như cũ khi thành công
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));

            //BẮT BadCredentialsException
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user {}: Bad credentials", loginRequest.getUsername());
            // Trả về lỗi 401 với thông báo tiếng Việt Sử dụng cấu trúc ApiResponse
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false); // Thêm success=false vì dùng ApiResponse
            errorResponse.put("message", "Tài khoản hoặc mật khẩu không chính xác. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (AuthenticationException e) {
            // Bắt các lỗi AuthenticationException khác
            log.warn("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi xác thực: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }


    @Autowired
    PasswordEncoder encoder;
    @GetMapping("/{plainPassword}")
    public String hashPassword(@PathVariable String plainPassword) {
        return encoder.encode(plainPassword);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return authService.registerUser(userCreateRequest);
    }


    // --- THÊM ENDPOINT ĐẶT LẠI MẬT KHẨU ---
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request); // Gọi service để xử lý
            return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công."));
        } catch (ResourceNotFoundException e) {
            // Trường hợp không tìm thấy user hoặc email không khớp
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            // Các lỗi khác
            log.error("Error resetting password for user {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi hệ thống khi đặt lại mật khẩu."));
        }
    }


}
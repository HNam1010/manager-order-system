package com.example.auth.service.serviceimpl;

// ... imports ...
import com.example.auth.dto.reponse.JwtResponse;
import com.example.auth.dto.reponse.MessageResponse;
import com.example.auth.dto.request.LoginRequest;
import com.example.auth.dto.request.UserCreateRequest;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtils;
import com.example.auth.security.UserDetailsImpl;
import com.example.auth.service.servicerepo.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtils jwtUtils;

    @Override
    public ResponseEntity<?> registerUser(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByUsername(userCreateRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }
        if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Tạo user mới
        User user = new User();
        user.setUsername(userCreateRequest.getUsername());
        user.setEmail(userCreateRequest.getEmail());
        user.setPassword(encoder.encode(userCreateRequest.getPassword())); // Mã hóa password

        // Gán Role (Mặc định là CUSTOMER nếu không chỉ định)
        String strRole = userCreateRequest.getRole();
        Role userRole;

        if (strRole == null || strRole.isBlank()) {
            userRole = roleRepository.findByName("CUSTOMER") // Hoặc tên Role chuẩn của bạn
                    .orElseThrow(() -> new RuntimeException("Error: Role CUSTOMER is not found."));
        } else {
            // Xử lý nếu frontend gửi tên role (vd: "admin")
            switch (strRole.toLowerCase()) {
                case "admin":
                    userRole = roleRepository.findByName("ADMIN")
                            .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
                    break;
                default:
                    userRole = roleRepository.findByName("CUSTOMER")
                            .orElseThrow(() -> new RuntimeException("Error: Role CUSTOMER is not found."));
            }
        }
        user.setRole(userRole); // Gán role
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Override
    public ResponseEntity<JwtResponse> authenticateUser(LoginRequest loginRequest) {
        // Xác thực bằng AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // Set Authentication vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Tạo JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Lấy UserDetails từ Authentication object
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Lấy danh sách role từ authorities
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Trả về JwtResponse
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }
}
package com.example.auth.controller;

import com.example.auth.dto.PageDTO;
import com.example.auth.dto.request.UserProfileUpdateRequest;
import com.example.auth.dto.request.UserUpdateRequest;
import com.example.auth.dto.reponse.UserResponse;
import com.example.auth.security.UserDetailsImpl;
import com.example.auth.service.servicerepo.UserService;
import com.example.be.commons.ApiResponse;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam; // Thêm import

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // --- GET Lấy danh sách User
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageDTO<UserResponse>>> getAllUsers(
            @PageableDefault(page = 0, size = 10, sort = "username") Pageable pageable,
            // lọc loại người dùng
           @RequestParam(name = "roleId", required = false) Integer roleId
    ) {

        // LẤY THÔNG TIN ADMIN ĐANG ĐĂNG NHẬP
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentAdminUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            currentAdminUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        } else {
            // Trường hợp này không nên xảy ra nếu @PreAuthorize hoạt động đúng, nhưng nên log lại
            log.warn("Could not determine current admin user ID from SecurityContext inside getAllUsers.");
            // Có thể quyết định trả về lỗi hoặc tiếp tục mà không lọc
        }

        log.info("API GET /users - Called by ADMIN (ID: {}). RoleId Filter: {}", currentAdminUserId, roleId);

        // TRUYỀN ID ADMIN XUỐNG SERVICE
        Page<UserResponse> userPage = userService.getAllUsers(pageable, roleId, currentAdminUserId); // Thêm currentAdminUserId

        PageDTO<UserResponse> pageDTO = new PageDTO<>(userPage);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công.", pageDTO));
    }

    //GET Lấy chi tiết User
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {

        log.info("API GET /users/{} - Called by ADMIN", id);

        UserResponse user = userService.getUserResponseById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công.", user));
    }


    //  PUT Cập nhật User
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest updateRequest) {

        log.info("API PUT /users/{} - Called by ADMIN", id);

        UserResponse updatedUser = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công.", updatedUser));
    }

    //  DELETE Xóa User
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id) {

        log.info("API DELETE /users/{} - Called by ADMIN", id);

        // Nếu service cần biết ai là người xóa, lấy từ SecurityContextHolder bên trong service
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công."));
    }


    // API cho người dùng tự lấy thông tin
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        UserResponse userResponse = userService.getUserResponseByUsername(currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công.", userResponse));
    }



    //  API cho người dùng tự cập nhật thông tin
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
            currentUserId = currentUserDetails.getId();
            log.debug("Updating profile for user ID obtained from UserDetailsImpl: {}", currentUserId);
        } else {
            log.error("Cannot determine current user ID. Principal type: {}", (authentication != null && authentication.getPrincipal() != null) ? authentication.getPrincipal().getClass().getName() : "null");
            throw new IllegalStateException("Không thể xác định người dùng hiện tại để cập nhật.");
        }
        if (currentUserId == null) {
            throw new IllegalStateException("Không thể xác định người dùng hiện tại để cập nhật (ID is null).");
        }
        log.info("Calling updateUserProfile for userId: {}", currentUserId);
        UserResponse updatedUser = userService.updateUserProfile(currentUserId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công.", updatedUser));
    }
}
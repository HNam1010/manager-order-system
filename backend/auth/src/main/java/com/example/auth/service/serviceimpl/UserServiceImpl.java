package com.example.auth.service.serviceimpl;

import com.example.auth.dto.request.ResetPasswordRequest;
import com.example.auth.dto.request.UserUpdateRequest;
import com.example.auth.dto.reponse.UserResponse;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.mapper.UserMapper; // Import UserMapper
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.servicerepo.UserService;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication; // <-- THÊM IMPORT
import org.springframework.security.core.context.SecurityContextHolder; // <-- THÊM IMPORT
import com.example.auth.security.UserDetailsImpl; // <-- Import UserDetailsImpl
import com.example.auth.dto.request.UserProfileUpdateRequest; // Import DTO mới

import com.example.be.commons.handler.exception.BadRequestException;
import com.example.be.commons.handler.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Có thể dùng
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import StringUtils

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserMapper userMapper; // Inject UserMapper

    // Inject PasswordEncoder nếu cho phép đặt lại mật khẩu
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true) // readOnly cho các thao tác chỉ đọc
    public Page<UserResponse> getAllUsers(Pageable pageable, Integer roleId, Long currentAdminUserId) {

        log.info("Fetching users. Pageable: {}, RoleId filter: {}, Excluding admin ID: {}",
                pageable, roleId, currentAdminUserId);

        // 1. Lấy Page<User> từ Repository (vẫn lấy cả admin)
        Page<User> userEntityPage;
        if (roleId != null) {
            userEntityPage = userRepository.findByRole_SerialId(roleId, pageable);
            log.debug("Filtered users by roleId: {}", roleId);
        } else {
            userEntityPage = userRepository.findAll(pageable);
            log.debug("Fetched all users without role filter.");
        }

        // 2. Lọc bỏ Admin hiện tại khỏi danh sách content
        List<User> filteredUserList = userEntityPage.getContent(); // Lấy list gốc
        if (currentAdminUserId != null) {
            filteredUserList = userEntityPage.getContent().stream()
                    .filter(user -> !user.getSerialId().equals(currentAdminUserId)) // Loại bỏ user có ID trùng
                    .collect(Collectors.toList());
            log.debug("Filtered out current admin (ID: {}). Original count: {}, Filtered count: {}",
                    currentAdminUserId, userEntityPage.getContent().size(), filteredUserList.size());
        }

        // 3. Map danh sách đã lọc sang UserResponse
        List<UserResponse> userResponseList = filteredUserList.stream()
                .map(userMapper::toUserResponse) // Sử dụng mapper
                .collect(Collectors.toList());

        // 4. Tạo một đối tượng Page<UserResponse> mới với content đã lọc và thông tin phân trang từ page gốc trả về PageImpl
        return new PageImpl<>(
                userResponseList,                  // Content đã lọc và map
                userEntityPage.getPageable(),      // Pageable gốc
                userEntityPage.getTotalElements() // Vẫn giữ tổng gốc hoặc tính lại
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserResponseById(Long userId) {
        log.info("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest updateRequest) {
        log.info("Updating user with ID: {}. Request: {}", userId, updateRequest);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Cập nhật Email (nếu có và khác email hiện tại)
        if (StringUtils.hasText(updateRequest.getEmail()) && !existingUser.getEmail().equalsIgnoreCase(updateRequest.getEmail())) {
            userRepository.findByEmail(updateRequest.getEmail()).ifPresent(userWithSameEmail -> {
                if (!userWithSameEmail.getSerialId().equals(userId)) {
                    throw new BadRequestException("Email '" + updateRequest.getEmail() + "' đã được sử dụng bởi tài khoản khác.");
                }
            });
            existingUser.setEmail(updateRequest.getEmail());
            log.debug("Updated email for user ID: {}", userId);
        }

        // Cập nhật Role (nếu có và khác role hiện tại)
        if (StringUtils.hasText(updateRequest.getRole()) && !existingUser.getRole().getName().equalsIgnoreCase(updateRequest.getRole())) {
            String newRoleName = updateRequest.getRole().toUpperCase(); // Chuẩn hóa tên Role
            Role newRole = roleRepository.findByName(newRoleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với tên: " + newRoleName));
            existingUser.setRole(newRole);
            log.debug("Updated role for user ID: {} to {}", userId, newRoleName);
        }

        if (StringUtils.hasText(updateRequest.getBirthDay())) { // Kiểm tra hợp lệ
            existingUser.setBirthDay(updateRequest.getBirthDay());
            log.debug("Updated birthDay for user ID: {}", userId);
        }

        if (StringUtils.hasText(updateRequest.getAddress())) {
            existingUser.setAddress(updateRequest.getAddress());
            log.debug("Updated address for user ID: {}", userId);
        }


        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully with ID: {}", userId);
        return userMapper.toUserResponse(updatedUser);
    }


    @Override
    @Transactional
    public void deleteUser(Long idOfUserToDelete) { // Đổi tên tham số cho rõ ràng hơn
        log.info("Attempting to delete user with ID: {}", idOfUserToDelete);

        //LẤY THÔNG TIN USER ĐANG ĐĂNG NHẬP
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        String currentUsername = null;

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
            currentUserId = currentUserDetails.getId(); // Lấy ID user đang thực hiện
            currentUsername = currentUserDetails.getUsername();
            log.debug("Delete request initiated by user - ID: {}, Username: {}", currentUserId, currentUsername);
        } else {
            log.error("Could not retrieve current user information from SecurityContext for delete operation.");
            throw new IllegalStateException("Không thể xác định người dùng đang thực hiện yêu cầu xóa.");
        }

        //KIỂM TRA XÓA CHÍNH MÌNH
        // Sử dụng currentUserId đã lấy được
        if (currentUserId != null && currentUserId.equals(idOfUserToDelete)) {
            log.warn("Attempt to delete own account by user ID: {}", currentUserId);
            throw new BadRequestException("Bạn không thể xóa chính tài khoản của mình.");
        }

        User userToDelete = userRepository.findById(idOfUserToDelete)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + idOfUserToDelete));

        //KIỂM TRA XÓA ADMIN CUỐI CÙNG
        if ("ADMIN".equalsIgnoreCase(userToDelete.getRole().getName())) { // Dùng equalsIgnoreCase
            long adminCount = userRepository.countByRole_Name("ADMIN"); //phương thức này tồn tại và không phân biệt hoa thường
            if (adminCount <= 1) {
                log.error("Attempt to delete the last ADMIN user (ID: {}) by user ID: {}", idOfUserToDelete, currentUserId);
                throw new BadRequestException("Không thể xóa quản trị viên cuối cùng của hệ thống.");
            }
            log.warn("ADMIN user ({}) is being deleted by user ID: {}", userToDelete.getUsername(), currentUserId);
        }

        userRepository.deleteById(idOfUserToDelete); // Sửa lại dùng deleteById nếu chưa
        log.info("User with ID: {} deleted successfully by user ID: {}", idOfUserToDelete, currentUserId);
    }


    @Override
    @Transactional
    public UserResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        log.info("Updating profile for user ID: {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId + " để cập nhật profile."));

        boolean updated = false;

        // Cập nhật Email nếu được cung cấp và khác email hiện tại
        if (StringUtils.hasText(request.getEmail()) && !currentUser.getEmail().equalsIgnoreCase(request.getEmail())) {
            // Kiểm tra email mới có trùng với user khác không
            userRepository.findByEmail(request.getEmail()).ifPresent(otherUser -> {
                if (!otherUser.getSerialId().equals(userId)) {
                    throw new BadRequestException("Email '" + request.getEmail() + "' đã được sử dụng.");
                }
            });
            currentUser.setEmail(request.getEmail());
            log.debug("Profile update: Email changed for user ID: {}", userId);
            updated = true;
        }

        // Cập nhật các trường khác được phép (Ví dụ: address, phone, birthDay)
        if (StringUtils.hasText(request.getPhone())) {
            currentUser.setPhone(request.getPhone());
            log.debug("Profile update: Phone changed for user ID: {}", userId);
            updated = true;
        }
        if (StringUtils.hasText(request.getAddress())) {
            currentUser.setAddress(request.getAddress());
            log.debug("Profile update: Address changed for user ID: {}", userId);
            updated = true;
        }
        if (StringUtils.hasText(request.getBirthDay())) {
            currentUser.setBirthDay(request.getBirthDay());
            log.debug("Profile update: BirthDay changed for user ID: {}", userId);
            updated = true;
        }

        // Cập nhật mật khẩu nếu được cung cấp sử dụng thêm currentPassword và newPassword vào DTO
        if (StringUtils.hasText(request.getNewPassword())) {
            if (!StringUtils.hasText(request.getCurrentPassword())) {
                throw new BadRequestException("Vui lòng nhập mật khẩu hiện tại để đổi mật khẩu mới.");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                throw new BadRequestException("Mật khẩu hiện tại không đúng.");
            }
            if (request.getNewPassword().length() < 6) { // kiểm tra độ dài
                throw new BadRequestException("Mật khẩu mới phải có ít nhất 6 ký tự.");
            }
            currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
            log.info("Profile update: Password changed for user ID: {}", userId);
            updated = true;
        }


        if (updated) {
            User savedUser = userRepository.save(currentUser);
            log.info("User profile updated successfully for ID: {}", userId);
            return userMapper.toUserResponse(savedUser);
        } else {
            log.info("No changes detected in user profile for ID: {}", userId);
            return userMapper.toUserResponse(currentUser); // Trả về thông tin hiện tại nếu không có gì thay đổi
        }
    }

    @Override
    @Transactional(readOnly = true) // Thêm readOnly nếu chỉ đọc
    public UserResponse getUserResponseByUsername(String username) {
        log.debug("Fetching user details for username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));
        // Sử dụng UserMapper để chuyển đổi User entity sang UserResponse DTO
        return userMapper.toUserResponse(user);
    }


    @Override
    @Transactional // Đảm bảo tính nhất quán
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Attempting to reset password for username: {}", request.getUsername());

        // Tìm user bằng username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với username: " + request.getUsername()));

        // Kiểm tra email có khớp không (không phân biệt hoa thường)
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            log.warn("Email mismatch for user {}. Provided: {}, Expected: {}", request.getUsername(), request.getEmail(), user.getEmail());
            throw new ResourceNotFoundException("Thông tin username hoặc email không chính xác."); // Trả về lỗi chung để tránh lộ thông tin
        }

        // Băm mật khẩu mới
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());

        // Cập nhật mật khẩu đã băm
        user.setPassword(hashedPassword);

        // Lưu thay đổi
        userRepository.save(user);
        log.info("Password reset successfully for username: {}", request.getUsername());
    }


}
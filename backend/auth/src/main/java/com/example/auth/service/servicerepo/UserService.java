package com.example.auth.service.servicerepo;

import com.example.auth.dto.request.ResetPasswordRequest;
import com.example.auth.dto.request.UserProfileUpdateRequest;
import com.example.auth.dto.request.UserUpdateRequest;
import com.example.auth.dto.reponse.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {


     //Lấy danh sách tất cả người dùng (phân trang) - cho Admin. , Integer roleId
     Page<UserResponse> getAllUsers(Pageable pageable, Integer roleId, Long currentAdminUserId);


     // Lấy thông tin chi tiết người dùng theo ID - cho Admin.
    UserResponse getUserResponseById(Long userId);


     //Cập nhật thông tin người dùng (Admin).
    UserResponse updateUser(Long userId, UserUpdateRequest updateRequest);


      //Xóa người dùng (Admin).
    void deleteUser(Long userIdToDelete); // Nhận ID người xóa và người bị xóa //, Long currentUserId

    //sửa thông tin
    UserResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);


    UserResponse getUserResponseByUsername(String username);

    //reset pass
    void resetPassword(ResetPasswordRequest request);



}

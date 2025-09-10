package com.example.auth.repository;


import com.example.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Tìm user theo username để login
    Optional<User> findByEmail(String email); // Tìm user theo email
    Boolean existsByUsername(String username); // Kiểm tra username tồn tại
    Boolean existsByEmail(String email); // Kiểm tra email tồn tại

    // Đếm số user theo tên Role (cần thiết cho việc kiểm tra xóa admin cuối)
    long countByRole_Name(String roleName);


    //lọc role
    Page<User> findByRole_SerialId(Integer roleId, Pageable pageable);


}
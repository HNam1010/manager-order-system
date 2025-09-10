package com.example.auth.service.serviceimpl;

import com.example.auth.dto.reponse.RoleResponse; // Import DTO
import com.example.auth.entity.Role;
import com.example.auth.mapper.RoleMapper; // *** GIẢ SỬ BẠN CÓ ROLEMAPPER ***
import com.example.auth.repository.RoleRepository;
import com.example.auth.service.servicerepo.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors; // Import Collectors

@Service
public class RoleServiceImpl implements RoleService { // Implement đúng interface

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper; // *** INJECT ROLEMAPPER ***

    @Override // Override phương thức từ interface
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() { // Sửa tên và kiểu trả về cho khớp
        List<Role> roles = roleRepository.findAll(); // Lấy danh sách Role Entity
        // Chuyển đổi List<Role> thành List<RoleResponse> bằng Mapper
        return roles.stream()
                .map(roleMapper::toRoleResponse) // Sử dụng mapper để chuyển đổi từng Role
                .collect(Collectors.toList());
    }
}
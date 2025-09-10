package com.example.auth.controller;


import com.example.auth.dto.reponse.RoleResponse;

import com.example.auth.service.servicerepo.RoleService;
import com.example.be.commons.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Import các annotation

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        log.info("API GET /roles");
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vai trò thành công.", roles));
    }
}
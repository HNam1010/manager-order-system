package com.example.auth.mapper;


import com.example.auth.dto.reponse.RoleResponse;
import com.example.auth.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List; // Import List nếu cần map list

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(source = "serialId", target = "serialId")
    @Mapping(source = "name", target = "name")
    RoleResponse toRoleResponse(Role role);

    // map danh sách
    List<RoleResponse> toRoleResponseList(List<Role> roles);
}
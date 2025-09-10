package com.example.auth.mapper;

import com.example.auth.dto.reponse.UserResponse;
import com.example.auth.entity.Role; // Import Role
import com.example.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections; // Import Collections
import java.util.List;
import java.util.stream.Collectors; // Import Collectors

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(source = "serialId", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRole()))")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "birthDay", target = "birthDay")
    UserResponse toUserResponse(User user);

    @Named("mapRoles")
    default List<String> mapRoles(Role role) {
        if (role == null || role.getName() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(role.getName());
    }
}

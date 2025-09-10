package com.example.auth.service.servicerepo;

import com.example.auth.dto.reponse.RoleResponse;
import java.util.List;

public interface RoleService {
    List<RoleResponse> getAllRoles();
}

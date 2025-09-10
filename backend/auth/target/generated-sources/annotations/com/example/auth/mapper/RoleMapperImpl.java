package com.example.auth.mapper;

import com.example.auth.dto.reponse.RoleResponse;
import com.example.auth.entity.Role;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-09T16:01:00+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleResponse toRoleResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleResponse roleResponse = new RoleResponse();

        roleResponse.setSerialId( role.getSerialId() );
        roleResponse.setName( role.getName() );

        return roleResponse;
    }

    @Override
    public List<RoleResponse> toRoleResponseList(List<Role> roles) {
        if ( roles == null ) {
            return null;
        }

        List<RoleResponse> list = new ArrayList<RoleResponse>( roles.size() );
        for ( Role role : roles ) {
            list.add( toRoleResponse( role ) );
        }

        return list;
    }
}

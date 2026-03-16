package com.example.security.security;

import com.example.security.entity.type.PermissionType;
import com.example.security.entity.type.RoleType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.security.entity.type.PermissionType.*;
import static com.example.security.entity.type.RoleType.*;

public class RolePermissionMapping {

    private static final Map<RoleType, Set<PermissionType>> map = Map.of(
            PATIENT,Set.of(PATIENT_READ,PATIENT_WRITE),
            DOCTOR,Set.of(PATIENT_WRITE),
            ADMIN,Set.of(USER_MANAGE,REPORT_VIEW)
    );

    public static Set<SimpleGrantedAuthority> getAuthoritiesForRole(RoleType roleType) {
        return map.get(roleType).stream()
                .map(permission->new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
    }
}

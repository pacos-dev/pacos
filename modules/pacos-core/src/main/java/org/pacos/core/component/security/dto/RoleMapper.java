package org.pacos.core.component.security.dto;

import org.pacos.core.component.security.domain.Role;

public class RoleMapper {

    private RoleMapper() {
    }

    public static RoleDTO map(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setLabel(role.getLabel());
        roleDTO.setDescription(role.getDescription());
        return roleDTO;

    }
}

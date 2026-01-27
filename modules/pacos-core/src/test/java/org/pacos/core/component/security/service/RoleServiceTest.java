package org.pacos.core.component.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;

class RoleServiceTest extends IntegrationTestContext {

    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void whenSaveRoleTheIsPersist() {
        long initCount = roleRepository.count();
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setLabel("myRole2");
        roleDTO.setDescription("description");
        //when
        roleService.createRole(roleDTO);
        List<RoleDTO> roleDTOList = roleService.loadAllRoles();
        //then
        assertEquals(initCount + 1, roleRepository.count());
        assertEquals(initCount + 1, roleDTOList.size());
    }
}
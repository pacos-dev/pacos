package org.pacos.core.component.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.repository.RoleRepository;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

    @Test
    void whenCreateRoleWithExistingLabelThenThrowPacosException() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setLabel("ADMIN");
        when(roleRepository.existsByLabelAndIdNot("ADMIN", null)).thenReturn(true);

        assertThrows(PacosException.class, () -> roleService.createRole(roleDTO));
    }

    @Test
    void whenCreateRoleWithIdThenUpdateExistingRole() {
        Role existing = new Role();
        existing.setId(12);
        existing.setLabel("OLD_LABEL");
        existing.setDescription("Old description");
        when(roleRepository.existsByLabelAndIdNot("updated", 12)).thenReturn(false);
        when(roleRepository.findById(12)).thenReturn(java.util.Optional.of(existing));
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(12);
        roleDTO.setLabel("updated");
        roleDTO.setDescription("New description");

        roleService.createRole(roleDTO);

        assertEquals("UPDATED", existing.getLabel());
        assertEquals("New description", existing.getDescription());
        Mockito.verify(roleRepository).save(existing);
    }

    @Test
    void whenCreateRoleWithUnknownIdThenCreateRoleWithSuppliedIdAndDescription() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(13);
        roleDTO.setLabel("new_role");
        roleDTO.setDescription("Description");
        when(roleRepository.existsByLabelAndIdNot("new_role", 13)).thenReturn(false);
        when(roleRepository.findById(13)).thenReturn(java.util.Optional.empty());

        roleService.createRole(roleDTO);

        Mockito.verify(roleRepository).save(Mockito.argThat(role ->
                "NEW_ROLE".equals(role.getLabel()) && "Description".equals(role.getDescription())));
    }

    @Test
    void whenRemoveRoleDoesNotExistThenDoNotDeleteRole() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(12);
        when(roleRepository.findById(12)).thenReturn(java.util.Optional.empty());

        roleService.removeRole(roleDTO);

        Mockito.verify(roleRepository, Mockito.never()).delete(Mockito.any());
    }

    @Test
    void whenCloneRoleExistsThenCopyLabelDescriptionAndPermissions() {
        Role source = new Role();
        source.setId(12);
        source.setLabel("TEST");
        source.setDescription("Source description");
        roleRepository.save(source);
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(12);
        roleDTO.setDescription("Cloned description");
        when(roleRepository.findById(12)).thenReturn(java.util.Optional.of(source));
        when(roleRepository.count()).thenReturn(5L);

        roleService.cloneRole(roleDTO);

        Mockito.verify(roleRepository).save(Mockito.argThat(copy ->
                "TEST_COPY_5".equals(copy.getLabel())
                        && "Cloned description".equals(copy.getDescription())
                        && copy.getAppPermissions().equals(source.getAppPermissions())));
    }

    @Test
    void whenCloneRoleDoesNotExistThenDoNotSaveRole() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(404);
        when(roleRepository.findById(404)).thenReturn(java.util.Optional.empty());

        roleService.cloneRole(roleDTO);

        Mockito.verify(roleRepository, Mockito.never()).save(Mockito.any());
    }

}
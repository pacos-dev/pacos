package org.pacos.core.component.security.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.repository.RoleRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void whenCreateRoleThenPersistUppercaseLabelAndDescription() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setLabel("myRole2");
        roleDTO.setDescription("description");
        when(roleRepository.existsByLabelAndIdNot("myRole2", null)).thenReturn(false);

        roleService.createRole(roleDTO);

        verify(roleRepository).save(argThat(role ->
                "MYROLE2".equals(role.getLabel()) && "description".equals(role.getDescription())));
    }

    @Test
    void whenCreateRoleWithExistingLabelThenThrowPacosException() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setLabel("ADMIN");
        when(roleRepository.existsByLabelAndIdNot("ADMIN", null)).thenReturn(true);

        assertThrows(PacosException.class, () -> roleService.createRole(roleDTO));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void whenCreateRoleWithIdThenUpdateExistingRole() {
        Role existing = new Role();
        existing.setId(12);
        existing.setLabel("OLD_LABEL");
        existing.setDescription("Old description");
        when(roleRepository.existsByLabelAndIdNot("updated", 12)).thenReturn(false);
        when(roleRepository.findById(12)).thenReturn(Optional.of(existing));
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(12);
        roleDTO.setLabel("updated");
        roleDTO.setDescription("New description");

        roleService.createRole(roleDTO);

        assertEquals("UPDATED", existing.getLabel());
        assertEquals("New description", existing.getDescription());
        verify(roleRepository).save(existing);
    }

    @Test
    void whenCreateRoleWithUnknownIdThenCreateNewRoleWithDescription() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(13);
        roleDTO.setLabel("new_role");
        roleDTO.setDescription("Description");
        when(roleRepository.existsByLabelAndIdNot("new_role", 13)).thenReturn(false);
        when(roleRepository.findById(13)).thenReturn(Optional.empty());

        roleService.createRole(roleDTO);

        verify(roleRepository).save(argThat(role ->
                "NEW_ROLE".equals(role.getLabel()) && "Description".equals(role.getDescription())));
    }

    @Test
    void whenCreateRoleWithNullDescriptionThenKeepExistingDescription() {
        Role existing = new Role();
        existing.setId(7);
        existing.setDescription("Existing description");
        when(roleRepository.existsByLabelAndIdNot("updated", 7)).thenReturn(false);
        when(roleRepository.findById(7)).thenReturn(Optional.of(existing));
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(7);
        roleDTO.setLabel("updated");

        roleService.createRole(roleDTO);

        assertEquals("Existing description", existing.getDescription());
    }

    @Test
    void whenLoadAllRolesThenMapRepositoryRows() {
        Role role = new Role();
        role.setId(3);
        role.setLabel("ADMIN");
        when(roleRepository.findAllByOrderByLabelAsc()).thenReturn(List.of(role));

        List<RoleDTO> result = roleService.loadAllRoles();

        assertEquals(1, result.size());
        assertEquals("ADMIN", result.getFirst().getLabel());
    }

    @Test
    void whenCountRoleAssignmentThenReturnRepositoryCount() {
        when(roleRepository.countRoleAssignment(14)).thenReturn(3);

        assertEquals(3, roleService.countRoleAssignment(14));
    }

    @Test
    void whenRemoveExistingRoleThenDeleteIt() {
        Role role = new Role();
        role.setId(12);
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(12);
        when(roleRepository.findById(12)).thenReturn(Optional.of(role));

        roleService.removeRole(roleDTO);

        verify(roleRepository).delete(role);
    }

    @Test
    void whenRemoveMissingRoleThenDoNotDeleteAnything() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(12);
        when(roleRepository.findById(12)).thenReturn(Optional.empty());

        roleService.removeRole(roleDTO);

        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void whenCloneExistingRoleThenCopyLabelDescriptionAndPermissions() {
        Role source = new Role();
        source.setId(12);
        source.setLabel("TEST");
        source.setDescription("Source description");
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(12);
        roleDTO.setDescription("Cloned description");
        when(roleRepository.findById(12)).thenReturn(Optional.of(source));
        when(roleRepository.count()).thenReturn(5L);

        roleService.cloneRole(roleDTO);

        verify(roleRepository).save(argThat(copy ->
                "TEST_COPY_5".equals(copy.getLabel())
                        && "Cloned description".equals(copy.getDescription())
                        && copy.getAppPermissions().equals(source.getAppPermissions())));
    }

    @Test
    void whenCloneRoleWithLongLabelThenTruncateCopiedLabel() {
        Role source = new Role();
        source.setId(12);
        source.setLabel("X".repeat(252));
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(12);
        roleDTO.setDescription("description");
        when(roleRepository.findById(12)).thenReturn(Optional.of(source));
        when(roleRepository.count()).thenReturn(100L);

        roleService.cloneRole(roleDTO);

        verify(roleRepository).save(argThat(copy -> copy.getLabel().length() == 254));
    }

    @Test
    void whenCloneMissingRoleThenDoNotSaveAnything() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(404);
        when(roleRepository.findById(404)).thenReturn(Optional.empty());

        roleService.cloneRole(roleDTO);

        verify(roleRepository, never()).save(any(Role.class));
    }
}

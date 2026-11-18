package org.pacos.core.component.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.security.domain.AppPermission;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class RoleServiceUnitTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void whenCreateRoleThenLabelIsUppercaseAndDescriptionIsPersisted() {
        RoleDTO roleDTO = roleDTO(null, "developer", "Backend developer");
        when(roleRepository.existsByLabelAndIdNot("developer", null)).thenReturn(false);

        roleService.createRole(roleDTO);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertEquals("DEVELOPER", captor.getValue().getLabel());
        assertEquals("Backend developer", captor.getValue().getDescription());
    }

    @Test
    void whenCreateExistingRoleThenExceptionIsThrown() {
        RoleDTO roleDTO = roleDTO(null, "admin", "Administrator");
        when(roleRepository.existsByLabelAndIdNot("admin", null)).thenReturn(true);

        assertThrows(PacosException.class, () -> roleService.createRole(roleDTO));

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void whenUpdateRoleThenExistingRoleIsUpdated() {
        Role existingRole = new Role();
        existingRole.setId(12);
        existingRole.setLabel("OLD_LABEL");
        existingRole.setDescription("Old description");
        RoleDTO roleDTO = roleDTO(12, "operator", "Updated description");
        when(roleRepository.existsByLabelAndIdNot("operator", 12)).thenReturn(false);
        when(roleRepository.findById(12)).thenReturn(Optional.of(existingRole));

        roleService.createRole(roleDTO);

        verify(roleRepository).save(existingRole);
        assertEquals("OPERATOR", existingRole.getLabel());
        assertEquals("Updated description", existingRole.getDescription());
    }

    @Test
    void whenLoadAllRolesThenRolesAreMappedToDtos() {
        Role role = new Role();
        role.setId(3);
        role.setLabel("ADMIN");
        role.setDescription("Administrator");
        when(roleRepository.findAllByOrderByLabelAsc()).thenReturn(List.of(role));

        List<RoleDTO> result = roleService.loadAllRoles();

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getId());
        assertEquals("ADMIN", result.get(0).getLabel());
        assertEquals("Administrator", result.get(0).getDescription());
    }

    @Test
    void whenCountRoleAssignmentThenRepositoryCountIsReturned() {
        when(roleRepository.countRoleAssignment(7)).thenReturn(4);

        int result = roleService.countRoleAssignment(7);

        assertEquals(4, result);
    }

    @Test
    void whenRemoveExistingRoleThenRoleIsDeleted() {
        Role role = new Role();
        role.setId(7);
        when(roleRepository.findById(7)).thenReturn(Optional.of(role));

        roleService.removeRole(roleDTO(7, "ADMIN", "Administrator"));

        verify(roleRepository).delete(role);
    }

    @Test
    void whenRemoveMissingRoleThenNoRoleIsDeleted() {
        when(roleRepository.findById(7)).thenReturn(Optional.empty());

        roleService.removeRole(roleDTO(7, "ADMIN", "Administrator"));

        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void whenCloneRoleThenPermissionsAndDescriptionAreCopied() {
        Role source = new Role();
        source.setLabel("ADMIN");
        Set<AppPermission> permissions = new HashSet<>();
        permissions.add(new AppPermission());
        source.setAppPermissions(permissions);
        when(roleRepository.findById(7)).thenReturn(Optional.of(source));
        when(roleRepository.count()).thenReturn(4L);

        roleService.cloneRole(roleDTO(7, "ADMIN", "Cloned description"));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertEquals("ADMIN_COPY_4", captor.getValue().getLabel());
        assertEquals("Cloned description", captor.getValue().getDescription());
        assertEquals(permissions, captor.getValue().getAppPermissions());
    }

    @Test
    void whenCloneRoleWithLongLabelThenClonedLabelIsTruncated() {
        Role source = new Role();
        source.setLabel("A".repeat(250));
        when(roleRepository.findById(7)).thenReturn(Optional.of(source));
        when(roleRepository.count()).thenReturn(12345L);

        roleService.cloneRole(roleDTO(7, "ADMIN", "Cloned description"));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertEquals(254, captor.getValue().getLabel().length());
    }

    private RoleDTO roleDTO(Integer id, String label, String description) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(id);
        roleDTO.setLabel(label);
        roleDTO.setDescription(description);
        return roleDTO;
    }
}

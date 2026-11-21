package org.pacos.core.component.security.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.security.PermissionName;
import org.pacos.core.component.security.domain.AppPermission;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.repository.PermissionRepository;
import org.pacos.core.component.security.repository.RoleRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void whenUserHasNoPermissionsThenReturnEmptySet() {
        when(permissionRepository.findAllByUserId(1)).thenReturn(Set.of());

        Set<PermissionName> result = permissionService.loadUserPermission(1, Set.of(3));

        assertEquals(Set.of(), result);
        verify(permissionRepository).findAllByUserId(1);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void whenRootRoleIsAssignedThenReturnAllPermissions() {
        AppPermission first = permission("user.read");
        AppPermission second = permission("user.write");
        when(permissionRepository.findAll()).thenReturn(List.of(first, second));

        Set<PermissionName> result = permissionService.loadUserPermission(1, Set.of(Role.ROOT_ROLE));

        assertEquals(Set.of(new PermissionName("user.read"), new PermissionName("user.write")), result);
        verify(permissionRepository).findAll();
    }

    @Test
    void whenUserHasPermissionsThenReturnDistinctNonNullPermissions() {
        when(permissionRepository.findAllByUserId(1)).thenReturn(
                new java.util.LinkedHashSet<>(java.util.Arrays.asList("user.read", null, "user.write", "user.read")));

        Set<PermissionName> result = permissionService.loadUserPermission(1, Set.of(3));

        assertEquals(Set.of(new PermissionName("user.read"), new PermissionName("user.write")), result);
    }

    private AppPermission permission(String key) {
        AppPermission permission = new AppPermission();
        permission.setKey(key);
        return permission;
    }
}

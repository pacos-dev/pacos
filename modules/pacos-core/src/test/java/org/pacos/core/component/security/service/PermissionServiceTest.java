package org.pacos.core.component.security.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.security.PermissionName;
import org.pacos.core.component.security.repository.PermissionRepository;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @InjectMocks
    private PermissionService permissionService;

    @Test
    void whenUserHasNoPermissionsThenReturnEmptySet() {
        int userId = 1;
        Set<Integer> roleIds = Set.of(2);

        Set<PermissionName> result = permissionService.loadUserPermission(userId, roleIds);

        assertTrue(result.isEmpty());
    }
}
package org.pacos.core.component.security.service;

import java.util.Set;

import lombok.Getter;
import org.config.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.security.Permission;
import org.pacos.base.session.AccessDecision;
import org.pacos.core.component.security.domain.PermissionDefaultConfig;
import org.pacos.core.component.security.domain.Permissions;
import org.pacos.core.component.security.domain.UserPermission;
import org.pacos.core.component.security.dto.PermissionDetailConfig;
import org.pacos.core.component.security.repository.PermissionDefaultConfigRepository;
import org.pacos.core.component.security.repository.PermissionRepository;
import org.pacos.core.component.security.repository.UserPermissionRepository;
import org.pacos.core.component.user.domain.User;
import org.pacos.core.component.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPermissionServiceTest extends IntegrationTestContext {

    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionDefaultConfigRepository defaultConfigRepository;
    @Autowired
    private UserPermissionService userPermissionService;
    @Autowired
    private UserPermissionRepository userPermissionRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        defaultConfigRepository.deleteAllInBatch();
        permissionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void whenLoadUserPermissionsThenLoadDefaultConfiguration() {
        //given
        Permissions action = permissionRepository.save(new Permissions(TestPermissions.TEST));
        defaultConfigRepository.save(new PermissionDefaultConfig(action));
        //when
        Set<PermissionDetailConfig> permissions = userPermissionService.getUserPermissionsWithDetails(1);
        //then
        assertEquals(1, permissions.size());
        assertEquals(AccessDecision.DENY, permissions.iterator().next().getDecision());
    }

    @Test
    void whenLoadUserPermissionsThenEnrichConfigurationAboutMissingEntries() {
        //given
        Permissions action = permissionRepository.save(new Permissions(TestPermissions.TEST));
        Permissions
                action2 = permissionRepository.save(new Permissions(TestPermissions.TEST2));
        defaultConfigRepository.save(new PermissionDefaultConfig(action));
        defaultConfigRepository.save(new PermissionDefaultConfig(action2));
        User user = new User();
        user.setLogin("test");
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        UserPermission entity = new UserPermission();
        entity.setUser(user);
        entity.setAction(action);
        entity.setDecision(AccessDecision.ALLOW);
        userPermissionRepository.save(entity);
        //when
        Set<PermissionDetailConfig> permissions = userPermissionService.getUserPermissionsWithDetails(1);
        //then
        assertEquals(2, permissions.size());
        assertTrue(permissions.stream().anyMatch(e -> e.getKey().equals("test")));
    }

    @Getter
    enum TestPermissions implements Permission {

        TEST("test", "test", "test",""),
        TEST2("test1", "test", "test","");

        private final String key;
        private final String label;
        private final String category;
        private final String description;

        TestPermissions(String key, String label, String category,String description) {
            this.key = key;
            this.label = label;
            this.category = category;
            this.description = description;
        }

    }
}
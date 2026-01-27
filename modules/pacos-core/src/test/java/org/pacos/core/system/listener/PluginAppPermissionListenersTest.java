package org.pacos.core.system.listener;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.security.Permission;
import org.pacos.core.component.security.service.PermissionService;
import org.springframework.context.ApplicationContext;

import lombok.Getter;

class PluginAppPermissionListenersTest {

    ApplicationContext context;
    PermissionService permissionService;
    ClassLoader classLoader;

    @Getter
    enum TestPermission implements Permission {

        A("test", "test", "test", ""),
        B("test1", "test", "test", "");

        private final String key;
        private final String label;
        private final String category;
        private final String description;

        TestPermission(String key, String label, String category, String description) {
            this.key = key;
            this.label = label;
            this.category = category;
            this.description = description;
        }

    }

    @BeforeEach
    void setup() {
        context = mock(ApplicationContext.class);
        permissionService = mock(PermissionService.class);
        classLoader = mock(ClassLoader.class);
        when(context.getClassLoader()).thenReturn(classLoader);
    }

    @Test
    void whenCallPluginInitializedThenPermissionsResolved() {
        try (MockedStatic<PluginPermissionsListeners> mocked = mockStatic(PluginPermissionsListeners.class,
                CALLS_REAL_METHODS)) {
            Set<Permission> perms = Set.of(TestPermission.A, TestPermission.B);
            mocked.when(() -> PluginPermissionsListeners.loadPermissions(context)).thenReturn(perms);

            new PluginPermissionsListeners(context, permissionService);

            verify(permissionService).resolvePermissionDefinition(perms);
        }
    }
}
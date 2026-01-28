package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.PermissionService;
import org.pacos.core.component.security.service.RoleService;
import org.springframework.test.util.ReflectionTestUtils;

import com.vaadin.flow.component.button.Button;

class RolesManagementTabViewTest {

    private RolesManagementTabView view;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = mock(RoleService.class);
        PermissionService permissionService = mock(PermissionService.class);

        when(roleService.loadAllRoles()).thenReturn(List.of());

        view = new RolesManagementTabView(roleService, permissionService);
    }

    @Test
    void whenCloneEventCalledThenInvokeServiceAndRefresh() {
        //given
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(10);

        //when
        ReflectionTestUtils.invokeMethod(view, "cloneEvent", roleDTO);

        //then
        verify(roleService).cloneRole(roleDTO);
        verify(roleService, atLeastOnce()).loadAllRoles();
    }

    @Test
    void whenCloneEventFailsThenShowNotification() {
        //given
        RoleDTO roleDTO = new RoleDTO();
        RuntimeException exception = new RuntimeException("Error");
        doThrow(exception).when(roleService).cloneRole(roleDTO);

        try (MockedStatic<NotificationUtils> mockedNotification = mockStatic(NotificationUtils.class)) {
            //when
            ReflectionTestUtils.invokeMethod(view, "cloneEvent", roleDTO);

            //then
            mockedNotification.verify(() -> NotificationUtils.error(exception));
        }
    }

    @Test
    void whenCreateRemoveButtonForRootRoleThenButtonIsDisabled() {
        //given
        RoleDTO rootRole = new RoleDTO();
        rootRole.setId(Role.ROOT_ROLE);

        //when
        Button result = (Button) ReflectionTestUtils.invokeMethod(view, "createRemoveButtonForRole", rootRole);

        //then
        assertNotNull(result);
        assertFalse(result.isEnabled());
    }

    @Test
    void whenCreateRemoveButtonForRegularRoleThenButtonIsEnabled() {
        //given
        RoleDTO regularRole = new RoleDTO();
        regularRole.setId(999);

        //when
        Button result = (Button) ReflectionTestUtils.invokeMethod(view, "createRemoveButtonForRole", regularRole);

        //then
        assertNotNull(result);
        assertTrue(result.isEnabled());
    }

    @Test
    void whenRemoveRoleEventCalledThenInvokeServiceAndRefresh() {
        //given
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(50);

        //when
        boolean result = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(view, "removeRoleEvent", roleDTO));

        //then
        assertTrue(result);
        verify(roleService).removeRole(roleDTO);
        verify(roleService, atLeastOnce()).loadAllRoles();
    }

    @Test
    void whenClonRolesButtonCreatedForRootThenReturnDisabledButton() {
        //given
        RoleDTO rootRole = new RoleDTO();
        rootRole.setId(Role.ROOT_ROLE);

        //when
        Button result = ReflectionTestUtils.invokeMethod(view, "clonRolesButton", rootRole);

        //then
        assertFalse(result.isEnabled());
    }

    @Test
    void whenEditPermissionsCreatedForRootThenReturnDisabledButton() {
        //given
        RoleDTO rootRole = new RoleDTO();
        rootRole.setId(Role.ROOT_ROLE);

        //when
        Button result = ReflectionTestUtils.invokeMethod(view, "editPermissions", rootRole);

        //then
        assertFalse(result.isEnabled());
    }
}
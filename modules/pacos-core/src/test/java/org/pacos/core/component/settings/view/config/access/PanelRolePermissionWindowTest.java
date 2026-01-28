package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.security.dto.AppPermissionConfig;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.PermissionService;
import org.springframework.test.util.ReflectionTestUtils;

class PanelRolePermissionWindowTest {

    private PanelRolePermissionWindow window;
    private RoleDTO roleDTO;
    private PermissionService permissionService;
    private RolePermissionWindowConfig moduleConfig;

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
        roleDTO = new RoleDTO();
        roleDTO.setId(100);

        permissionService = mock(PermissionService.class);
        moduleConfig = mock(RolePermissionWindowConfig.class);

        when(permissionService.loadPermissionsConfig(roleDTO)).thenReturn(List.of());

        window = new PanelRolePermissionWindow(moduleConfig, roleDTO, permissionService);
    }

    @Test
    void whenGetRoleDTOCalledThenReturnCorrectRole() {
        //when
        RoleDTO result = window.getRoleDTO();

        //then
        assertEquals(roleDTO, result);
    }

    @Test
    void whenModifyUserPermissionEventCalledWithTrueThenInvokeServiceSaveWithTrue() {
        //given
        AppPermissionConfig config = new AppPermissionConfig(1, "test", "test", "test", "test", false);
        boolean newValue = true;

        //when
        ReflectionTestUtils.invokeMethod(window, "modifyUserPermissionEvent", config, newValue);

        //then
        verify(permissionService).savePermissionState(1, true, 100);
    }

    @Test
    void whenModifyUserPermissionEventCalledWithFalseThenInvokeServiceSaveWithFalse() {
        //given
        AppPermissionConfig config = new AppPermissionConfig(2, "test", "test", "test", "test", false);
        boolean newValue = false;

        //when
        ReflectionTestUtils.invokeMethod(window, "modifyUserPermissionEvent", config, newValue);

        //then
        verify(permissionService).savePermissionState(2, false, 100);
    }

    @Test
    void whenWindowInitializedThenPermissionsAreLoadedFromService() {
        //then
        verify(permissionService).loadPermissionsConfig(roleDTO);
    }
}
package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.event.UISystem;
import org.pacos.base.exception.PacosException;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.PermissionService;
import org.pacos.core.component.security.service.RoleService;
import org.springframework.test.util.ReflectionTestUtils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.ValidationException;

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

    @Test
    void whenRoleBtnBarThenDivContainerWithButtons() {
        //given
        RoleDTO rootRole = new RoleDTO();
        rootRole.setId(Role.ROOT_ROLE);

        //when
        Div result = ReflectionTestUtils.invokeMethod(view, "roleBtnBar", rootRole);

        //then
        assertEquals(4, result.getChildren().filter(Button.class::isInstance).count());
    }

    @Test
    void whenEditPermissionsBtnClickThenOpenRolePermissionWindow() {
        //given
        RoleDTO rootRole = new RoleDTO();
        rootRole.setId(Role.ROOT_ROLE);
        UISystem system = VaadinMock.mockSystem();
        //when
        ReflectionTestUtils.invokeMethod(view, "openRolePermissionWindow", rootRole);
        //then
        verify(system.getWindowManager()).showWindow(any(PanelRolePermissionWindow.class));
    }

    @Test
    void whenShowConfirmationWindowThenCallWindowManager() {
        //given
        RoleDTO rootRole = new RoleDTO();
        rootRole.setId(Role.ROOT_ROLE);
        UISystem system = VaadinMock.mockSystem();
        //when
        ReflectionTestUtils.invokeMethod(view, "showConfirmationDialog", rootRole);
        //then
        verify(system.getWindowManager()).showModalWindow(any(ConfirmationWindowConfig.class));
    }

    @Test
    void whenShowNewDialogForRoleThenDialogIsOpened() {
        //given
        RoleDTO rootRole = new RoleDTO();
        rootRole.setId(Role.ROOT_ROLE);
        VaadinMock.mockSystem();
        //when
        Dialog dialog = view.showDialogWithFormEvent(rootRole);
        //then
        assertTrue(dialog.isOpened());
    }

    @Test
    void whenSaveRoleThenSaveValidFormAndCloseDialog() throws ValidationException {
        //given
        RoleDTO role = mock(RoleDTO.class);
        Dialog dialog = mock(Dialog.class);
        RoleForm form = mock(RoleForm.class);
        when(form.getBean()).thenReturn(role);
        when(form.validate()).thenReturn(true);
        //when
        view.saveRole(form, dialog);
        //then
        verify(roleService).createRole(role);
        verify(dialog).close();
    }

    @Test
    void whenSaveRoleAndFormIsNotValidThenDoNothing() throws ValidationException {
        //given
        RoleDTO role = mock(RoleDTO.class);
        Dialog dialog = mock(Dialog.class);
        RoleForm form = mock(RoleForm.class);
        when(form.getBean()).thenReturn(role);
        when(form.validate()).thenReturn(false);
        //when
        view.saveRole(form, dialog);
        //then
        verify(roleService).loadAllRoles();
        verifyNoMoreInteractions(roleService);
        verifyNoInteractions(dialog);
    }

    @Test
    void whenExceptionWhileSaveThenDoNotThrowException() throws ValidationException {
        //given
        RoleDTO role = mock(RoleDTO.class);
        Dialog dialog = mock(Dialog.class);
        RoleForm form = mock(RoleForm.class);
        when(form.getBean()).thenReturn(role);
        when(form.validate()).thenReturn(true);
        doThrow(PacosException.class).when(roleService).createRole(role);
        //when
        assertDoesNotThrow(() -> view.saveRole(form, dialog));
        //then
        verifyNoInteractions(dialog);
    }
}
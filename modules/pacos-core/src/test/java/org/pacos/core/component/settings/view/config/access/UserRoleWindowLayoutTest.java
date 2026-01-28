package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.common.event.OnSaveEvent;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.RoleService;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserService;
import org.springframework.test.util.ReflectionTestUtils;

import com.vaadin.flow.component.checkbox.Checkbox;

class UserRoleWindowLayoutTest {

    private UserRoleWindowLayout window;
    private UserService userService;
    private ShortUserDTO user;
    private OnSaveEvent<ShortUserDTO> onSaveEvent;
    private Set<RoleDTO> userRoles;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        VaadinMock.mockSystem();
        UserProxyService userProxyService = mock(UserProxyService.class);
        userService = mock(UserService.class);
        RoleService roleService = mock(RoleService.class);
        onSaveEvent = mock(OnSaveEvent.class);
        user = mock(ShortUserDTO.class);

        UserRoleWindowConfig config = mock(UserRoleWindowConfig.class);
        when(config.getShortUserDTO()).thenReturn(user);
        when(user.id()).thenReturn(1);

        when(userProxyService.getUserService()).thenReturn(userService);
        when(userProxyService.getRoleService()).thenReturn(roleService);

        userRoles = new HashSet<>();
        when(userService.loadRoles(1)).thenReturn(userRoles);

        window = new UserRoleWindowLayout(config, userProxyService, onSaveEvent);
    }

    @Test
    void whenCreateCheckBoxBtnForAssignedRoleThenCheckboxIsTrue() {
        //given
        RoleDTO role = new RoleDTO();
        role.setId(10);
        userRoles.add(role);

        //when
        Checkbox result = ReflectionTestUtils.invokeMethod(window, "createCheckBoxBtn", role);

        //then
        assertTrue(result.getValue());
    }

    @Test
    void whenCheckboxValueChangesToTrueThenRoleIsAddedAndServiceInvoked() {
        //given
        RoleDTO role = new RoleDTO();
        role.setId(20);
        Checkbox cb = (Checkbox) ReflectionTestUtils.invokeMethod(window, "createCheckBoxBtn", role);

        try (MockedStatic<NotificationUtils> mockedNotification = mockStatic(NotificationUtils.class)) {
            //when
            ReflectionTestUtils.invokeMethod(cb, "setModelValue", Boolean.TRUE, true);

            //then
            assertTrue(userRoles.contains(role));
            verify(userService).setRoles(userRoles, 1);
            verify(onSaveEvent).onSaveEvent(user);
            mockedNotification.verify(() -> NotificationUtils.success(anyString()));
        }
    }

    @Test
    void whenCheckboxValueChangesToFalseThenRoleIsRemovedAndServiceInvoked() {
        //given
        RoleDTO role = new RoleDTO();
        role.setId(30);
        userRoles.add(role);
        Checkbox cb = ReflectionTestUtils.invokeMethod(window, "createCheckBoxBtn", role);

        try (MockedStatic<NotificationUtils> mockedNotification = mockStatic(NotificationUtils.class)) {
            //when
            ReflectionTestUtils.invokeMethod(cb, "setModelValue", Boolean.FALSE, true);

            //then
            assertFalse(userRoles.contains(role));
            verify(userService).setRoles(userRoles, 1);
            verify(onSaveEvent).onSaveEvent(user);
            mockedNotification.verify(() -> NotificationUtils.success(anyString()));
        }
    }

    @Test
    void whenUpdateUserRolesCalledThenInvokeUserServiceAndNotification() {
        //given
        try (MockedStatic<NotificationUtils> mockedNotification = mockStatic(NotificationUtils.class)) {
            //when
            ReflectionTestUtils.invokeMethod(window, "updateUserRoles");

            //then
            verify(userService).setRoles(anySet(), eq(1));
            verify(onSaveEvent).onSaveEvent(user);
            mockedNotification.verify(() -> NotificationUtils.success("User roles updated"));
        }
    }
}
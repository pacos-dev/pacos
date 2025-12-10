package org.pacos.core.component.user.view.event;

import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.config.session.UserSessionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateNewUserEventTest {

    private UserProxyService userProxyService;

    @BeforeEach
    void setUp() {
        userProxyService = mock(UserProxyService.class);
    }

    @Test
    void whenLoginDoesNotExistThenCreateAccount() {
        UserForm form = new UserForm("newUser", "password", "password");
        UserDTO userDTO = new UserDTO(1, "newUser", null);

        when(userProxyService.checkIfLoginNoExists(form.getLogin())).thenReturn(true);
        when(userProxyService.createAccount(form)).thenReturn(userDTO);

        VaadinSession session = mock(VaadinSession.class);
        VaadinSession.setCurrent(session);
        when(session.getAttribute(UserSession.class)).thenReturn(new UserSession(userDTO));

        try (MockedStatic<UserSessionService> mock = mockStatic(UserSessionService.class)) {
            mock.when(UserSessionService::logOut).thenAnswer(inv -> null);
            UserDTO result = CreateNewUserEvent.fireEvent(userProxyService, form);

            assertNotNull(result);
            assertEquals(userDTO, result);
        }

        verify(userProxyService).checkIfLoginNoExists(form.getLogin());
        verify(userProxyService).createAccount(form);
    }

    @Test
    void whenLoginExistsThenDoNotCreateAccount() {
        UserForm form = new UserForm("existingUser", "password", "password");

        when(userProxyService.checkIfLoginNoExists(form.getLogin())).thenReturn(false);

        UserDTO result = CreateNewUserEvent.fireEvent(userProxyService, form);

        assertNull(result);
        verify(userProxyService).checkIfLoginNoExists(form.getLogin());
        verify(userProxyService, never()).createAccount(any(UserForm.class));
    }
}

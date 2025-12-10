package org.pacos.core.system.event;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.Binder;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.component.user.view.RegisterForm;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterEventTest {

    @Test
    void whenCreateAccountReturnsNullThenDoNotNavigate() {
        RegisterForm mockRegisterForm = mock(RegisterForm.class);
        Binder<UserForm> mockBinder = mock(Binder.class);
        UserProxyService mockUserProxyService = mock(UserProxyService.class);
        UI mockUi = mock(UI.class);

        when(mockRegisterForm.createAccount(mockBinder, mockUserProxyService)).thenReturn(null);

        RegisterEvent.fireEvent(mockRegisterForm, mockBinder, mockUserProxyService, mockUi);

        verify(mockUi, never()).navigate(anyString());
    }

    @Test
    void whenCreateAccountReturnsUserThenNavigateToLogin() {
        RegisterForm mockRegisterForm = mock(RegisterForm.class);
        Binder<UserForm> mockBinder = mock(Binder.class);
        UserProxyService mockUserProxyService = mock(UserProxyService.class);
        UI mockUi = mock(UI.class);
        UserDTO mockUserDTO = mock(UserDTO.class);

        when(mockRegisterForm.createAccount(mockBinder, mockUserProxyService)).thenReturn(mockUserDTO);

        RegisterEvent.fireEvent(mockRegisterForm, mockBinder, mockUserProxyService, mockUi);

        verify(mockUi, times(1)).navigate("login");
    }
}

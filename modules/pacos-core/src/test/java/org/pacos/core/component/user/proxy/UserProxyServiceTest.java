package org.pacos.core.component.user.proxy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.user.service.ChangePasswordForm;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.component.user.service.UserService;
import org.pacos.core.system.view.login.LoginForm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProxyServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserProxyService userProxyService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenUpdateUserVariableThenCalled() {
        UserDTO userDTO = new UserDTO(2, "admin", 0);
        doNothing().when(userService).updateVariableCollection(userDTO);
        userProxyService.updateUserVariableCollection(userDTO);
        verify(userService, times(1)).updateVariableCollection(userDTO);
    }

    @Test
    void whenCheckIfLoginNoExistsThenReturnResult() {
        String login = "testLogin";
        when(userService.isNotExistsByLogin(login)).thenReturn(true);
        boolean result = userProxyService.checkIfLoginNoExists(login);
        assertTrue(result);
        verify(userService, times(1)).isNotExistsByLogin(login);
    }

    @Test
    void whenIsValidCredentialsThenReturnResult() {
        LoginForm loginForm = new LoginForm();
        when(userService.isValidCredentials(loginForm)).thenReturn(true);
        boolean result = userProxyService.isValidCredentials(loginForm);
        assertTrue(result);
        verify(userService, times(1)).isValidCredentials(loginForm);
    }

    @Test
    void whenChangePasswordThenReturnResult() {
        LoginForm loginForm = new LoginForm();
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        when(userService.changePassword(loginForm, changePasswordForm)).thenReturn(true);
        boolean result = userProxyService.changePassword(loginForm, changePasswordForm);
        assertTrue(result);
        verify(userService, times(1)).changePassword(loginForm, changePasswordForm);
    }

    @Test
    void whenCreateAccountThenReturnUserDTO() {
        UserForm userForm = new UserForm();
        UserDTO userDTO = new UserDTO(2, "admin", 0);
        when(userService.createAccount(userForm)).thenReturn(userDTO);
        UserDTO result = userProxyService.createAccount(userForm);
        assertEquals(userDTO, result);
        verify(userService, times(1)).createAccount(userForm);
    }

    @Test
    void whenLoadUserByTokenThenReturnOptionalUserDTO() {
        String token = "testToken";
        Optional<UserDTO> userDTO = Optional.of(new UserDTO(2, "admin", 0));
        when(userService.loadUserByToken(token)).thenReturn(userDTO);
        Optional<UserDTO> result = userProxyService.loadUserByToken(token);
        assertEquals(userDTO, result);
        verify(userService, times(1)).loadUserByToken(token);
    }

    @Test
    void whenLoadUserByLoginThenReturnOptionalUserDTO() {
        String login = "testLogin";
        Optional<UserDTO> userDTO = Optional.of(new UserDTO(2, "admin", 0));
        when(userService.loadUserByLogin(login)).thenReturn(userDTO);
        Optional<UserDTO> result = userProxyService.loadUserByLogin(login);
        assertEquals(userDTO, result);
        verify(userService, times(1)).loadUserByLogin(login);
    }


    @Test
    void whenSaveTokenThenCalled() {
        String token = "testToken";
        UserDTO userDTO = new UserDTO(2, "admin", 0);
        doNothing().when(userService).saveToken(token, userDTO);
        userProxyService.saveToken(token, userDTO);
        verify(userService, times(1)).saveToken(token, userDTO);
    }
}

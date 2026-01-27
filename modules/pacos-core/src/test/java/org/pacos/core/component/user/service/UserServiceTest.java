package org.pacos.core.component.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pacos.base.exception.PacosException;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.dock.service.DockService;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.security.repository.RoleRepository;
import org.pacos.core.component.user.domain.User;
import org.pacos.core.component.user.repository.UserRepository;
import org.pacos.core.system.view.login.LoginForm;

class UserServiceTest {

    @Mock
    private UserRepository userServiceRepository;

    @Mock
    private DockService dockService;

    @Mock
    private RegistryProxy registryProxy;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenIsNotExistsByLoginThenReturnTrue() {
        String login = "testLogin";
        when(userServiceRepository.existsByLogin(login)).thenReturn(false);
        boolean result = userService.isNotExistsByLogin(login);
        assertTrue(result);
        verify(userServiceRepository, times(1)).existsByLogin(login);
    }

    @Test
    void whenCreateAccountThenReturnUserDTO() {
        UserForm userForm = new UserForm("login", "password", "password");
        when(userServiceRepository.findByLogin(userForm.getLogin())).thenReturn(Optional.empty());
        User user = new User();
        user.setId(2);
        user.setLogin("login");
        when(userServiceRepository.save(any(User.class))).thenReturn(user);
        when(registryProxy.readIntList(RegistryName.ONBOARD_ROLES)).thenReturn(List.of());
        UserDTO result = userService.createAccount(userForm);

        assertNotNull(result);
        assertEquals(userForm.getLogin(), result.getUserName());
        verify(userServiceRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenCreateAccountThenAssignOnboardingRoles() {
        UserForm userForm = new UserForm("login", "password", "password");
        when(userServiceRepository.findByLogin(userForm.getLogin())).thenReturn(Optional.empty());
        User user = new User();
        user.setId(2);
        user.setLogin("login");
        when(userServiceRepository.save(any(User.class))).thenReturn(user);
        when(registryProxy.readIntList(RegistryName.ONBOARD_ROLES)).thenReturn(List.of(1, 2));

        UserDTO result = userService.createAccount(userForm);

        assertNotNull(result);
        verify(roleRepository).findAllById(List.of(1, 2));
    }

    @Test
    void whenCreateAccountWithExistingLoginThenThrowException() {
        UserForm userForm = new UserForm("login", "password", "password");
        when(userServiceRepository.findByLogin(userForm.getLogin())).thenReturn(Optional.of(new User()));

        assertThrows(PacosException.class, () -> userService.createAccount(userForm));
        verify(userServiceRepository, never()).save(any(User.class));
    }

    @Test
    void whenIsValidCredentialsThenReturnTrue() {
        LoginForm loginForm = new LoginForm("login", "password");
        User user = new User();
        user.setPasswordHash(ShaHashAlgorithm.hashPassword("password"));

        when(userServiceRepository.findByLogin(loginForm.getLogin())).thenReturn(Optional.of(user));
        boolean result = userService.isValidCredentials(loginForm);

        assertTrue(result);
        verify(userServiceRepository, times(1)).findByLogin(loginForm.getLogin());
    }

    @Test
    void whenIsValidCredentialsThenReturnFalse() {
        LoginForm loginForm = new LoginForm("login", "wrongPassword");
        User user = new User();
        user.setPasswordHash(ShaHashAlgorithm.hashPassword("password"));

        when(userServiceRepository.findByLogin(loginForm.getLogin())).thenReturn(Optional.of(user));
        boolean result = userService.isValidCredentials(loginForm);

        assertFalse(result);
        verify(userServiceRepository, times(1)).findByLogin(loginForm.getLogin());
    }

    @Test
    void whenChangePasswordThenUpdatePassword() {
        LoginForm loginForm = new LoginForm("login", "password");
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        changePasswordForm.setNewPassword("newPassword");
        changePasswordForm.setCurrentPassword("password");
        User user = new User();
        user.setPasswordHash(ShaHashAlgorithm.hashPassword("password"));
        when(userServiceRepository.findByLogin(loginForm.getLogin())).thenReturn(Optional.of(user));
        when(userServiceRepository.save(user)).thenReturn(user);

        boolean result = userService.changePassword(loginForm, changePasswordForm);

        assertTrue(result);
        assertTrue(ShaHashAlgorithm.validatePassword(changePasswordForm.getNewPassword(), user.getPasswordHash()));
        verify(userServiceRepository, times(1)).save(user);
    }

    @Test
    void whenLoadUserByTokenThenReturnUserDTO() {
        String token = "tokenValue";
        User user = new User();
        user.setId(1);
        user.setLogin("login");
        user.setVariableCollectionId(100);

        when(userServiceRepository.findByToken(token)).thenReturn(Optional.of(user));
        Optional<UserDTO> result = userService.loadUserByToken(token);

        assertTrue(result.isPresent());
        assertEquals(user.getLogin(), result.get().getUserName());
        verify(userServiceRepository, times(1)).findByToken(token);
    }
}

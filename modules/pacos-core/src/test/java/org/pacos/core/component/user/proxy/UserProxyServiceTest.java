package org.pacos.core.component.user.proxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.security.PermissionName;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.security.service.PermissionService;
import org.pacos.core.component.security.service.RoleService;
import org.pacos.core.component.user.service.ChangePasswordForm;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.component.user.service.UserService;
import org.pacos.core.system.view.login.LoginForm;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProxyServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private RegistryProxy registryProxy;

    @InjectMocks
    private UserProxyService userProxyService;

    @Test
    void whenUpdateUserVariableThenCalled() {
        UserDTO userDTO = new UserDTO(2, "admin", 0);

        userProxyService.updateUserVariableCollection(userDTO);

        verify(userService).updateVariableCollection(userDTO);
    }

    @Test
    void whenCheckIfLoginNoExistsThenReturnResult() {
        String login = "testLogin";
        when(userService.isNotExistsByLogin(login)).thenReturn(true);

        assertTrue(userProxyService.checkIfLoginNoExists(login));
        verify(userService).isNotExistsByLogin(login);
    }

    @Test
    void whenIsValidCredentialsThenReturnResult() {
        LoginForm loginForm = new LoginForm();
        when(userService.isValidCredentials(loginForm)).thenReturn(true);

        assertTrue(userProxyService.isValidCredentials(loginForm));
        verify(userService).isValidCredentials(loginForm);
    }

    @Test
    void whenChangePasswordThenReturnResult() {
        LoginForm loginForm = new LoginForm();
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        when(userService.changePassword(loginForm, changePasswordForm)).thenReturn(true);

        assertTrue(userProxyService.changePassword(loginForm, changePasswordForm));
        verify(userService).changePassword(loginForm, changePasswordForm);
    }

    @Test
    void whenCreateAccountThenReturnUserDTO() {
        UserForm userForm = new UserForm();
        UserDTO userDTO = new UserDTO(2, "admin", 0);
        when(userService.createAccount(userForm)).thenReturn(userDTO);

        assertEquals(userDTO, userProxyService.createAccount(userForm));
        verify(userService).createAccount(userForm);
    }

    @Test
    void whenLoadUserByTokenThenReturnOptionalUserDTO() {
        String token = "testToken";
        Optional<UserDTO> userDTO = Optional.of(new UserDTO(2, "admin", 0));
        when(userService.loadUserByToken(token)).thenReturn(userDTO);

        assertEquals(userDTO, userProxyService.loadUserByToken(token));
        verify(userService).loadUserByToken(token);
    }

    @Test
    void whenLoadUserByLoginThenReturnOptionalUserDTO() {
        String login = "testLogin";
        Optional<UserDTO> userDTO = Optional.of(new UserDTO(2, "admin", 0));
        when(userService.loadUserByLogin(login)).thenReturn(userDTO);

        assertEquals(userDTO, userProxyService.loadUserByLogin(login));
        verify(userService).loadUserByLogin(login);
    }

    @Test
    void whenSaveTokenThenCalled() {
        String token = "testToken";
        UserDTO userDTO = new UserDTO(2, "admin", 0);

        userProxyService.saveToken(token, userDTO);

        verify(userService).saveToken(token, userDTO);
    }

    @Test
    void whenLoadUserRolesThenReturnAssignedRoles() {
        Set<Integer> roles = Set.of(2, 3);
        when(roleService.loadUserRoleIds(7)).thenReturn(roles);

        assertEquals(roles, userProxyService.loadUserRoles(7));
        verify(roleService).loadUserRoleIds(7);
    }

    @Test
    void whenLoadUserPermissionsThenReturnAssignedPermissions() {
        Set<PermissionName> permissions = Set.of(new PermissionName("user.read"));
        when(permissionService.loadUserPermission(7, Set.of(2))).thenReturn(permissions);

        assertEquals(permissions, userProxyService.loadUserPermissions(7, Set.of(2)));
        verify(permissionService).loadUserPermission(7, Set.of(2));
    }

    @Test
    void whenIsOnboardingDismissedThenReturnResult() {
        when(userService.isOnboardingDismissed(1)).thenReturn(true);

        assertTrue(userProxyService.isOnboardingDismissed(1));
        verify(userService).isOnboardingDismissed(1);
    }

    @Test
    void whenDismissOnboardingThenDelegateToUserService() {
        userProxyService.dismissOnboarding(1);

        verify(userService).dismissOnboarding(1);
    }
}

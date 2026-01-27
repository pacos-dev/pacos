package org.pacos.core.component.user.proxy;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.pacos.base.security.PermissionName;
import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.security.service.PermissionService;
import org.pacos.core.component.security.service.RoleService;
import org.pacos.core.component.user.service.ChangePasswordForm;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.component.user.service.UserService;
import org.pacos.core.system.view.login.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProxyService {

    private final UserService userService;
    private final RoleService roleService;
    private final RegistryProxy registryProxy;
    private final PermissionService permissionService;

    @Autowired
    public UserProxyService(UserService userService, RoleService roleService, RegistryProxy registryProxy,
                            PermissionService permissionService) {
        this.userService = userService;
        this.roleService = roleService;
        this.registryProxy = registryProxy;
        this.permissionService = permissionService;
    }
    public void updateUserVariableCollection(UserDTO userDTO) {
        userService.updateVariableCollection(userDTO);
    }

    public boolean checkIfLoginNoExists(String login) {
        return userService.isNotExistsByLogin(login);
    }

    public boolean isValidCredentials(LoginForm loginForm) {
        return userService.isValidCredentials(loginForm);
    }

    public boolean changePassword(LoginForm loginForm, ChangePasswordForm changePasswordForm) {
        return userService.changePassword(loginForm, changePasswordForm);
    }

    public UserDTO createAccount(UserForm form) {
        return userService.createAccount(form);
    }

    public Optional<UserDTO> loadUserByToken(String tokenValue) {
        return userService.loadUserByToken(tokenValue);
    }

    public Optional<UserDTO> loadUserByLogin(String login) {
        return userService.loadUserByLogin(login);
    }

    public void saveToken(String token, UserDTO user) {
        userService.saveToken(token, user);
    }

    public boolean isSingleUserMode() {
        return registryProxy.isSingleMode();
    }

    public List<ShortUserDTO> getAllUsers() {
        return userService.loadUsers();
    }

    public RoleService getRoleService() {
        return roleService;
    }

    public UserService getUserService() {
        return userService;
    }

    public Set<PermissionName> loadUserPermissions(int userId) {
        return permissionService.loadUserPermission(userId);
    }
}

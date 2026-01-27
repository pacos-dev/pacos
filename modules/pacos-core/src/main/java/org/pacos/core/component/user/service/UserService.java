package org.pacos.core.component.user.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pacos.base.exception.PacosException;
import org.pacos.base.session.ShortUserDTO;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.dock.service.DockService;
import org.pacos.core.component.dock.view.config.ActivatorConfig;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.dto.RoleMapper;
import org.pacos.core.component.security.repository.RoleRepository;
import org.pacos.core.component.user.domain.User;
import org.pacos.core.component.user.repository.UserRepository;
import org.pacos.core.system.view.login.LoginForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userServiceRepository;
    private RoleRepository roleRepository;
    private RegistryProxy registryProxy;
    private DockService dockService;

    public boolean isNotExistsByLogin(String userName) {
        return !userServiceRepository.existsByLogin(userName);
    }

    public UserDTO createAccount(UserForm userForm) {
        if (userServiceRepository.findByLogin(userForm.getLogin()).isPresent()) {
            throw new PacosException("Account with this login already exists");
        }

        User user = new User();
        user.setLogin(userForm.getLogin());
        user.setPasswordHash(ShaHashAlgorithm.hashPassword(userForm.getPassword()));

        List<Integer> onboardRolesIds = registryProxy.readIntList(RegistryName.ONBOARD_ROLES);
        if (!onboardRolesIds.isEmpty()) {
            List<Role> onboardRoles = roleRepository.findAllById(onboardRolesIds);
            user.setRoles(new HashSet<>(onboardRoles));
        }

        user = saveUserAndConfigureAccount(user);
        return new UserDTO(user.getId(), user.getLogin(), user.getVariableCollectionId());
    }

    public boolean isValidCredentials(LoginForm loginForm) {
        Optional<User> user = userServiceRepository.findByLogin(loginForm.getLogin());
        return user.filter(value -> ShaHashAlgorithm.validatePassword(loginForm.getPassword(), value.getPasswordHash()))
                .isPresent();
    }

    public Optional<UserDTO> loadUserByLogin(String login) {
        Optional<User> user = userServiceRepository.findByLogin(login);
        return user.map(value -> new UserDTO(value.getId(), value.getLogin(), value.getVariableCollectionId()));
    }

    public boolean changePassword(LoginForm loginForm, ChangePasswordForm changePasswordForm) {
        Optional<User> user = userServiceRepository.findByLogin(loginForm.getLogin());
        if (user.isPresent() && isValidCredentials(new LoginForm(loginForm.getLogin(),
                changePasswordForm.getCurrentPassword()))) {
            user.get().setPasswordHash(ShaHashAlgorithm.hashPassword(changePasswordForm.getNewPassword()));
            userServiceRepository.save(user.get());
            return true;
        }
        return false;
    }

    public Optional<UserDTO> loadUserByToken(String tokenValue) {
        Optional<User> user = userServiceRepository.findByToken(tokenValue);
        return user.map(value -> new UserDTO(value.getId(), value.getLogin(), value.getVariableCollectionId()));
    }

    private User saveUserAndConfigureAccount(User user) {
        user = userServiceRepository.save(user);

        Optional<ActivatorConfig> config =
                registryProxy.readRegistry(RegistryName.DEFAULT_DOCK_CONFIG, ActivatorConfig.class);
        if (config.isPresent()) {
            ActivatorConfig activatorConfig = config.get();
            for (String activator : activatorConfig.activators()) {
                dockService.addActivator(activator, user.getId());
            }
        }
        return user;
    }

    @Transactional("coreTransactionManager")
    public void updateVariableCollection(UserDTO user) {
        userServiceRepository.saveVariableCollection(user.getId(), user.getVariableCollectionId());
    }

    @Transactional("coreTransactionManager")
    public void saveToken(String token, UserDTO user) {
        userServiceRepository.saveToken(token, user.getId());
    }

    public List<ShortUserDTO> loadUsers() {
        return userServiceRepository.findAllWithRoles()
                .stream()
                .map(user -> new ShortUserDTO(user.getId(), user.getLogin(), user.getRoleList()))
                .toList();
    }

    @Transactional("coreTransactionManager")
    public void setRoles(Set<RoleDTO> rolesDTO, int userId) {
        User user = userServiceRepository.getReferenceById(userId);
        List<Role> roles = roleRepository.findAllById(rolesDTO.stream().map(RoleDTO::getId).toList());
        user.setRoles(new HashSet<>(roles));
        userServiceRepository.save(user);
    }

    @Transactional("coreTransactionManager")
    public void addRole(int roleId, int userId) {
        User user = userServiceRepository.getReferenceById(userId);
        roleRepository.findById(roleId).map(r -> user.getRoles().add(r));
        userServiceRepository.save(user);
    }

    @Transactional
    public Set<RoleDTO> loadRoles(Integer userId) {
        return userServiceRepository.getReferenceById(userId).getRoles().stream().map(RoleMapper::map).collect(Collectors.toSet());
    }
}

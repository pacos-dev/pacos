package org.pacos.core.component.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.exception.PacosException;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.dock.domain.DockConfiguration;
import org.pacos.core.component.dock.service.DockService;
import org.pacos.core.component.dock.view.config.ActivatorConfig;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.user.repository.UserRepository;
import org.pacos.core.system.view.login.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;


class UserServiceIntegrationTest extends IntegrationTestContext {

    @Autowired
    private UserRepository repository;
    @Autowired
    private UserService userService;
    @Autowired
    private RegistryProxy registryProxy;
    @Autowired
    private DockService dockService;
    @Autowired
    private EntityManager entityManager;

    private static final UserForm userForm = new UserForm("admin", "password", "password");
    private UserDTO userDTO;

    @BeforeEach
    void init() {
        userDTO = userService.createAccount(userForm);
    }

    @Test
    void whenVerifyIfLoginNotExistsThenReturnTrue() {
        assertTrue(userService.isNotExistsByLogin("guest"));
    }


    @Test
    void whenLoginAlreadyExistsThenReturnTrue() {
        assertFalse(userService.isNotExistsByLogin("admin"));
    }

    @Test
    void whenLoginAlreadyExistsThenThrowException() {
        assertThrows(PacosException.class, () -> userService.createAccount(userForm));
    }

    @Test
    void whenIsValidCredentialsThenReturnTrue() {
        assertTrue(userService.isValidCredentials(new LoginForm("admin", "password")));
    }

    @Test
    void whenIsValidCredentialsThenReturnFalse() {
        assertFalse(userService.isValidCredentials(new LoginForm("admin", "password2")));
    }

    @Test
    void whenLoadUserThenReturnEmptyResult() {
        assertFalse(userService.loadUserByLogin("guest").isPresent());
    }


    @Test
    void whenLoadUserThenReturnUserDTOResult() {
        Optional<UserDTO> userDTOOptional = userService.loadUserByLogin("admin");
        assertTrue(userDTOOptional.isPresent());
        assertEquals("admin", userDTOOptional.get().getUserName());
    }

    @Test
    void whenChangePasswordThenReturnTrue() {
        LoginForm loginForm = new LoginForm("admin", "password");
        ChangePasswordForm changePasswordForm = new ChangePasswordForm("password", "password2", "password2");

        assertTrue(userService.changePassword(loginForm, changePasswordForm));
    }

    @Test
    void whenChangePasswordAndCurrentOneIsInvalidThenReturnFalse() {
        LoginForm loginForm = new LoginForm("admin", "password");
        ChangePasswordForm changePasswordForm = new ChangePasswordForm("password2", "password2", "password2");

        assertFalse(userService.changePassword(loginForm, changePasswordForm));
    }

    @Test
    void whenChangePasswordAndUserDoesNotExistsThenReturnFalse() {
        LoginForm loginForm = new LoginForm("guest", "password");
        ChangePasswordForm changePasswordForm = new ChangePasswordForm("guest", "password2", "password2");

        assertFalse(userService.changePassword(loginForm, changePasswordForm));
    }


    @Test
    void whenLoadUserByTokenThenReturnUser() {
        userService.saveToken("token", userDTO);
        //when
        Optional<UserDTO> token = userService.loadUserByToken("token");
        assertTrue(token.isPresent());
        assertEquals(userDTO, token.get());
    }

    @Test
    void whenLoadUserByTokenThenDoNotReturnUser() {
        userService.saveToken("token", userDTO);
        //when
        Optional<UserDTO> token = userService.loadUserByToken("guest");
        assertFalse(token.isPresent());
    }

    @Test
    void whenDockConfigurationExistsThenNewAccountWillContainsDockConfiguration() {
        registryProxy.saveRegistry(RegistryName.DEFAULT_DOCK_CONFIG,
                new ActivatorConfig(List.of("test", "activator")));
        //when
        UserForm form = new UserForm("ada", "pass", "pass");
        UserDTO userDTO1 = userService.createAccount(form);
        List<DockConfiguration> configurations = dockService.findByUserIdOrderByOrderNum(userDTO1.getId());
        //then
        assertEquals(2, configurations.size());
        assertTrue(configurations.stream().anyMatch(e->e.getActivatorClass().equals("test")));
        assertTrue(configurations.stream().anyMatch(e->e.getActivatorClass().equals("activator")));
    }

    @Test
    void whenUpdateCollectionIdThenStoreNewValue() {
        userDTO.setVariableCollectionId(100);
        //when
        userService.updateVariableCollection(userDTO);
        entityManager.clear();
        //then
        assertEquals(100, repository.getReferenceById(userDTO.getId()).getVariableCollectionId());
    }

    @Test
    void whenLoadUsersThenReturnShortUserDtoList(){
        assertEquals(1,userService.loadUsers().size());
        assertEquals("admin",userService.loadUsers().get(0).name());
    }


}
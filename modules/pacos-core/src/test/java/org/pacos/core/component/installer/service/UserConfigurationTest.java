package org.pacos.core.component.installer.service;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.repository.UserRepository;
import org.pacos.core.component.user.service.UserForm;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserConfigurationTest extends IntegrationTestContext {

    @Autowired
    private UserConfiguration userConfiguration;
    @Autowired
    private UserProxyService userProxyService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void whenConfigureAccountThenDefineAdminIfUserNotSpecified() {
        //given
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.SINGLE);
        //when
        UserDTO userDTO = userConfiguration.configure(settings);
        //then
        assertEquals("admin", userDTO.getUserName());
        assertTrue(userRepository.existsByLogin("admin"));
    }

    @Test
    void whenConfigureAccountThenDefineUserAccount() {
        //given
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.MULTI);
        settings.getAccountData().setLogin("user");
        settings.getAccountData().setPassword("passwd");
        settings.getAccountData().setRepeatPassword("passwd");
        //when
        UserDTO userDTO = userConfiguration.configure(settings);
        //then
        assertEquals("user", userDTO.getUserName());
        assertTrue(userRepository.existsByLogin("user"));
        assertFalse(userRepository.existsByLogin("admin"));
    }

    @Test
    void whenUserAlreadyExistsWhileConfigureThenReturnExistingOne() {
        //given
        UserForm form = new UserForm();
        form.setLogin("user");
        form.setPassword("passwd");
        form.setRepeatPassword("passwd");
        userProxyService.createAccount(form);

        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.MULTI);
        settings.getAccountData().setLogin("user");
        settings.getAccountData().setPassword("passwd");
        settings.getAccountData().setRepeatPassword("passwd");
        //when
        UserDTO userDTO = userConfiguration.configure(settings);
        //then
        assertEquals("user", userDTO.getUserName());
        assertTrue(userRepository.existsByLogin("user"));
        assertTrue(userRepository.existsByLogin("guest"));
        assertEquals(2, userRepository.count());
    }


}
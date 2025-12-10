package org.pacos.core.component.installer.service;

import java.util.List;
import java.util.Optional;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.PanelSettings;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.variable.view.PanelVariable;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstallerServiceTest extends IntegrationTestContext {

    @Autowired
    private InstallerService installerService;
    @Autowired
    private UserProxyService userProxyService;

    @Autowired
    private DockServiceProxy dockServiceProxy;

    @Autowired
    private RegistryProxy registryProxy;

    @Autowired
    private PluginProxy pluginProxy;

    @Test
    void whenGetProxyThenReturnProxyInstance() {
        assertEquals(pluginProxy, installerService.getPluginProxy());
    }
    @Test
    void whenConfigureSystemThenCreateUserAdminAccount() {
        //given
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.SINGLE);
        //when
        installerService.configure(settings, e -> {
        });
        //then
        Optional<UserDTO> user = userProxyService.loadUserByLogin("admin");
        assertTrue(user.isPresent());
    }

    @Test
    void whenConfigureSystemThenCreateUserAccount() {
        //given
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.MULTI);
        settings.getAccountData().setLogin("user");
        settings.getAccountData().setPassword("pass");
        settings.getAccountData().setRepeatPassword("pass");
        //when
        installerService.configure(settings, e -> {
        });
        //then
        Optional<UserDTO> user = userProxyService.loadUserByLogin("user");
        assertTrue(user.isPresent());
    }

    @Test
    void whenConfigureSystemThenSetDefaultAppInDoc() {
        //given
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.SINGLE);
        //when
        installerService.configure(settings, e -> {
        });
        UserDTO userDTO = userProxyService.loadUserByLogin("admin").orElse(new UserDTO("admin"));

        //then
        List<DockConfigurationDTO> dockConfigList = dockServiceProxy.loadConfigurations(userDTO.getId());
        assertFalse(dockConfigList.isEmpty());
        assertTrue(dockConfigList.stream().anyMatch(c -> c.getActivator().equals(PanelSettings.class.getSimpleName())));
        assertTrue(dockConfigList.stream().anyMatch(c -> c.getActivator().equals(PanelVariable.class.getSimpleName())));
    }

    @Test
    void whenConfigureSystemThenSystemIsMarkedAsInstalled() {
        //given
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.SINGLE);
        //when
        installerService.configure(settings, e -> {
        });
        //then
        assertEquals("true",registryProxy.readRegistry(RegistryName.INSTALLED).orElse(""));
    }

    @Test
    void whenConfigureSingleModeThenRegistryIsSetToSingleMode() {
        //given
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.SINGLE);
        //when
        installerService.configure(settings, e -> {
        });
        //then
        assertEquals("true",registryProxy.readRegistry(RegistryName.SINGLE_MODE).orElse(""));
    }

    @Test
    void whenConfigureMultiModeThenRegistryIsNotSingleMode() {
        //given
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.MULTI);
        settings.getAccountData().setLogin("user");
        settings.getAccountData().setPassword("pass");
        settings.getAccountData().setRepeatPassword("pass");
        //when
        installerService.configure(settings, e -> {
        });
        //then
        assertEquals("false",registryProxy.readRegistry(RegistryName.SINGLE_MODE).orElse(""));
    }


}
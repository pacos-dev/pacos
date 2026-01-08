package org.pacos.core.component.settings.view.tab;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.system.service.AutoUpdateService;
import org.pacos.core.system.service.data.SystemUpdateResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SystemUpdateTabLayoutTest {

    private RegistryProxy registryProxy;
    private AutoUpdateService autoUpdateService;

    @BeforeEach
    void setUp() {
        this.registryProxy = Mockito.mock(RegistryProxy.class);
        this.autoUpdateService = Mockito.mock(AutoUpdateService.class);

        when(registryProxy.readBoolean(RegistryName.AUTO_UPDATE_ENABLED, false)).thenReturn(false);
        when(registryProxy.readRegistry(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, Integer.class, 0)).thenReturn(0);
        when(registryProxy.isSystemToUpdate()).thenReturn(false);

        when(registryProxy.readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0")).thenReturn("1.0");
        when(registryProxy.readRegistryOrDefault(RegistryName.AVAILABLE_SYSTEM_VERSION, "1.1")).thenReturn("1.0");
        when(registryProxy.readTime(RegistryName.LAST_UPDATE_TIME, "---")).thenReturn("---");
        when(registryProxy.readTime(RegistryName.LAST_UPDATE_CHECK_TIME, "---")).thenReturn("---");
    }

    @Test
    void whenInitializeWhenSystemNotToUpdateThenNoExceptions() {
        assertDoesNotThrow(() -> new SystemUpdateTabLayout(registryProxy, autoUpdateService));
    }

    @Test
    void whenInitializeWhenSystemToUpdateThenNoExceptions() {
        when(registryProxy.isSystemToUpdate()).thenReturn(true);
        when(registryProxy.readRegistry(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, Integer.class, 0)).thenReturn(1);

        assertDoesNotThrow(() -> new SystemUpdateTabLayout(registryProxy, autoUpdateService));
    }

    @Test
    void whenCheckUpdateTrueThenNotifySystem() {
        when(autoUpdateService.checkIfUpdateAvailable()).thenReturn(true);
        VaadinMock.mockSystem();
        //when
        new SystemUpdateTabLayout(registryProxy, autoUpdateService).checkUpdateBtnClickEvent();
        //then
        verify(UISystem.getCurrent()).notify(ModuleEvent.UPDATE_AVAILABLE);
    }

    @Test
    void whenCheckUpdateFalseThenShowNotification() {
        when(autoUpdateService.checkIfUpdateAvailable()).thenReturn(false);
        //when
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.info(any())).thenAnswer(invocationOnMock -> null);
            new SystemUpdateTabLayout(registryProxy, autoUpdateService).checkUpdateBtnClickEvent();
            //then
            utils.verify(() -> NotificationUtils.info(any()));
        }
    }

    @Test
    void whenUserSetCheckBoxToTrueThenSaveAutoUpdateConfiguration() {
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.success(any())).thenAnswer(invocationOnMock -> null);
            //when
            new SystemUpdateTabLayout(registryProxy, autoUpdateService).saveRegistryValue(RegistryName.AUTO_UPDATE_ENABLED, true);
            //then
            verify(registryProxy).saveRegistry(RegistryName.AUTO_UPDATE_ENABLED, true);
        }
    }

    @Test
    void whenUserSetCheckBoxToFalseThenSaveAutoUpdateConfiguration() {
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.success(any())).thenAnswer(invocationOnMock -> null);
            //when
            new SystemUpdateTabLayout(registryProxy, autoUpdateService).saveRegistryValue(RegistryName.AUTO_UPDATE_ENABLED, false);
            //then
            verify(registryProxy).saveRegistry(RegistryName.AUTO_UPDATE_ENABLED, false);
        }
    }

    @Test
    void whenUpdateSystemWithoutErrorThenRestartMachine() {
        VaadinMock.mockSystem();
        SystemUpdateResult updateResult = new SystemUpdateResult("1.0", "1.1");
        when(autoUpdateService.updateSystem(true)).thenReturn(Optional.of(updateResult));
        //when
        new SystemUpdateTabLayout(registryProxy, autoUpdateService).updateSystemBtnClickEvent();
        //then
        verify(registryProxy).saveRegistry(RegistryName.RESTART_REQUIRED, true);
        verify(UISystem.getCurrent()).notify(ModuleEvent.RESTART_REQUIRED);
    }

    @Test
    void whenNoUpdateSystemThenShowNotification() {
        VaadinMock.mockSystem();
        SystemUpdateResult updateResult = new SystemUpdateResult("1.0", "1.0");
        when(autoUpdateService.updateSystem(true)).thenReturn(Optional.of(updateResult));
        //when
        try (MockedStatic<NotificationUtils> utils = mockStatic(NotificationUtils.class)) {
            utils.when(() -> NotificationUtils.info(any())).thenAnswer(invocationOnMock -> null);
            //when
            new SystemUpdateTabLayout(registryProxy, autoUpdateService).updateSystemBtnClickEvent();
            utils.verify(() -> NotificationUtils.info(any()));
        }

    }

    @Test
    void whenGetSearchIndexThenReturnNotEmptyString(){
        assertNotNull(SystemUpdateTabLayout.getSearchIndex());
    }


}
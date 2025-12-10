package org.pacos.core.system.window;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.system.window.config.ReleaseNoteConfig;
import org.pacos.core.system.window.config.SystemInfoConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemInfoConfigPanelTest {

    @Test
    void whenInitializeThenNoException() {
        VaadinMock.mockSystem();
        RegistryProxy registryProxy = ProxyMock.registryProxy();
        assertDoesNotThrow(() -> new SystemInfoConfigPanel(new SystemInfoConfig(), registryProxy));
    }

    @Test
    void whenClickReleaseNoteThenOpenReleaseNoteWindowAndCloseInfoWindow() {
        VaadinMock.mockSystem();
        RegistryProxy registryProxy = ProxyMock.registryProxy();
        UISystem uiSystem = Mockito.mock(UISystem.class);
        when(uiSystem.getWindowManager()).thenReturn(Mockito.mock(WindowManager.class));
        SystemInfoConfigPanel panel = new SystemInfoConfigPanel(new SystemInfoConfig(), registryProxy);
        //when
        panel.openReleaseNotWindow(uiSystem);
        //then
        verify(uiSystem).notify(ModuleEvent.MODULE_SHUTDOWN, panel);
        verify(uiSystem.getWindowManager()).showWindow(ReleaseNoteConfig.class);
    }
}
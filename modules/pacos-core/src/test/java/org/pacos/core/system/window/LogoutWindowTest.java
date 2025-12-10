package org.pacos.core.system.window;

import java.util.List;

import org.config.VaadinMock;
import org.config.util.TestWindow;
import org.config.util.TestWindowConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.system.window.config.LogoutWindowConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogoutWindowTest {

    private RegistryProxy registryProxy;

    @BeforeEach
    void setUp() {
        registryProxy = Mockito.mock(RegistryProxy.class);
        when(registryProxy.isRestartRequired()).thenReturn(true);
    }

    @Test
    void whenInitializeAndNoActiveWindowsThenNoException() {
        VaadinMock.mockSystem();
        assertDoesNotThrow(() -> new LogoutWindow(new LogoutWindowConfig(), registryProxy));
    }

    @Test
    void whenInitializeAndActiveWindowsThenNoException() {
        UISystem system = VaadinMock.mockSystem();
        WindowManager windowManager = system.getWindowManager();

        List<DesktopWindow> desktopWindowList = List.of(new TestWindow(new TestWindowConfig()));
        when(windowManager.getAllInitializedWindows())
                .thenReturn(desktopWindowList);
        assertDoesNotThrow(() -> new LogoutWindow(new LogoutWindowConfig(), registryProxy));
    }

    @Test
    void whenCancelThenShutDownModule() {
        UISystem uiSystem = VaadinMock.mockSystem();
        LogoutWindow window = new LogoutWindow(new LogoutWindowConfig(), registryProxy);
        //when
        window.onCancel();

        verify(uiSystem).notify(ModuleEvent.MODULE_SHUTDOWN, window);
    }
}
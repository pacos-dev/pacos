package org.pacos.core.component.plugin.event;

import java.util.concurrent.CompletableFuture;

import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pacos.base.exception.PacosException;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.ModalWindow;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.proxy.PluginProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemovePluginEventTest {

    @Test
    void whenRemovePluginEventThenShowConfirmationWindow() {
        VaadinMock.mockSystem();
        PluginProxy proxy = ProxyMock.pluginProxyMock();
        //when
        RemovePluginEvent.fireEvent(proxy, new PluginDTO(), () -> {
        });
        //then
        ArgumentCaptor<ModalWindowConfig> windowConfigArgumentCaptor = ArgumentCaptor.forClass(ModalWindowConfig.class);

        WindowManager windowManager = UserSession.getCurrent().getUISystem().getWindowManager();
        verify(windowManager).showModalWindow(windowConfigArgumentCaptor.capture());
        assertEquals(ModalWindow.class, windowConfigArgumentCaptor.getValue().activatorClass());
    }

    @Test
    void whenPluginStoppedThenTriggerRemoveService() {
        VaadinMock.mockSystem();
        PluginProxy proxy = ProxyMock.pluginProxyMock();
        PluginDTO pluginDTO = Mockito.mock(PluginDTO.class);

        OnRemoveFinishEvent event = Mockito.mock(OnRemoveFinishEvent.class);
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        when(proxy.getPluginManager().stopPlugin(pluginDTO)).thenReturn(future);
        try (MockedStatic<PluginState> state = mockStatic(PluginState.class)) {
            state.when(() -> PluginState.canRun(pluginDTO)).thenReturn(true);
            //when
            boolean result = RemovePluginEvent.onConfirmEvent(proxy, pluginDTO, event);
            //then
            future.join();
            verify(proxy.getPluginService()).removePlugin(pluginDTO);
            assertTrue(result);
        }

    }

    @Test
    void whenPluginStoppedAndErrorWhileRemovingThenReturnTrue() {
        VaadinMock.mockSystem();
        PluginProxy proxy = ProxyMock.pluginProxyMock();
        PluginDTO pluginDTO = Mockito.mock(PluginDTO.class);

        OnRemoveFinishEvent event = Mockito.mock(OnRemoveFinishEvent.class);
        doThrow(PacosException.class).when(proxy.getPluginService()).removePlugin(pluginDTO);
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        when(proxy.getPluginManager().stopPlugin(pluginDTO)).thenReturn(future);

        try (MockedStatic<PluginState> state = mockStatic(PluginState.class)) {
            state.when(() -> PluginState.canRun(pluginDTO)).thenReturn(true);
            //when
            boolean result = RemovePluginEvent.onConfirmEvent(proxy, pluginDTO, event);
            //then
            verify(proxy.getPluginService()).removePlugin(pluginDTO);
            assertTrue(result);
        }
    }
}
package org.pacos.core.component.dock.view.dock;

import org.config.PluginManagerMock;
import org.config.VaadinMock;
import org.config.util.TestWindow;
import org.config.util.TestWindowConfig;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

class OsDockWrapperTest {

    @Test
    void whenInitializeDockThenNoException() {
        VaadinMock.mockSystem();
        DockServiceProxy proxy = Mockito.mock(DockServiceProxy.class);
        DockConfigurationDTO configurationDTO = new DockConfigurationDTO(1);
        configurationDTO.setActivator(TestWindow.class.getName());
        configurationDTO.setUserId(1);
        configurationDTO.setOrderNum(1);
        when(proxy.loadConfigurations(1)).thenReturn(List.of(configurationDTO));

        PluginManagerMock.mockPluginResources(Map.of("testWindow", new TestWindowConfig()));
        //when
        assertDoesNotThrow(() -> new OsDockWrapper(proxy));
    }

}
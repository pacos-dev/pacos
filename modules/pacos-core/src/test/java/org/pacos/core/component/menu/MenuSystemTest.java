package org.pacos.core.component.menu;

import java.util.List;
import java.util.Map;

import org.config.PluginManagerMock;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.config.util.TestWindowConfig;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.system.proxy.AppProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MenuSystemTest {

    @Test
    void whenBuildMenuThenNoException() {
        VaadinMock.mockSystem();

        AppProxy appProxy = ProxyMock.appProxyMock();
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder()
                .global(true).userId(2).build();
        when(appProxy.getUserVariableCollectionProxy().loadUserCollections(any())).thenReturn(List.of(collectionDTO));
        when(appProxy.getRegistryProxy().isSystemToUpdate()).thenReturn(true);

        PluginManagerMock.mockPluginResources(Map.of("testWindow", new TestWindowConfig()));
        //then
        assertDoesNotThrow(() -> new MenuSystem(appProxy));
    }
}
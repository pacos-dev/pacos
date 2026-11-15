package org.pacos.core.component.menu;

import java.util.List;
import java.util.Map;

import org.config.PluginManagerMock;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.config.util.TestWindowConfig;
import org.pacos.base.session.UserSession;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.view.plugin.VariablePlugin;
import org.pacos.core.system.proxy.AppProxy;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.CurrentInstance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        MenuSystem menuSystem = new MenuSystem(appProxy);

        assertDoesNotThrow(() -> menuSystem);
        assertNotNull(ComponentUtil.getData(UI.getCurrent(), VariablePlugin.class));
        assertNull(UserSession.getCurrent().getFromSession("variablePlugin"));
    }

    @Test
    void whenMenuIsCreatedThenVariablePluginIsNotSharedWithAnotherUi() {
        VaadinMock.mockSystem();

        AppProxy appProxy = ProxyMock.appProxyMock();
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder()
                .global(true).userId(2).build();
        when(appProxy.getUserVariableCollectionProxy().loadUserCollections(any())).thenReturn(List.of(collectionDTO));
        when(appProxy.getRegistryProxy().isSystemToUpdate()).thenReturn(true);
        PluginManagerMock.mockPluginResources(Map.of("testWindow", new TestWindowConfig()));

        new MenuSystem(appProxy);
        UI firstUi = UI.getCurrent();
        assertNotNull(ComponentUtil.getData(firstUi, VariablePlugin.class));

        UI secondUi = new UI();
        CurrentInstance.set(UI.class, secondUi);

        assertNull(ComponentUtil.getData(secondUi, VariablePlugin.class));
    }
}
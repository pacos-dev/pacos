package org.pacos.core.component.plugin.view.window.tab;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.config.ProxyMock;
import org.config.VaadinMock;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.repository.domain.PluginRepository;
import org.pacos.core.component.repository.service.PluginRepositoryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketplaceViewTest {

    @Test
    void whenInitializedThenNoException() {
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        VaadinMock.mockSystem();

        assertDoesNotThrow(() -> new MarketplaceView(pluginProxy));
    }

    @Test
    void whenRepositoryIsSelectedThenRepositoryServiceSelectsIt() {
        VaadinMock.mockSystem();
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        PluginRepositoryService repositoryService = pluginProxy.getPluginRepositoryService();
        PluginRepository selected = repository(1L);
        PluginRepository next = repository(2L);
        when(repositoryService.findAll()).thenReturn(List.of(selected, next));
        when(repositoryService.getSelectedRepository()).thenReturn(selected);
        when(repositoryService.toAppRepository(selected)).thenReturn(AppRepository.pluginRepo());
        when(repositoryService.toAppRepository(next)).thenReturn(AppRepository.pluginRepo());

        MarketplaceView view = new MarketplaceView(pluginProxy);
        ComboBox<PluginRepository> selector = findSelector(view);

        selector.setValue(next);

        verify(repositoryService).selectRepository(2L);
        assertEquals(next, selector.getValue());
    }

    @Test
    void whenRepositorySelectionFailsThenPreviousRepositoryIsRestored() {
        VaadinMock.mockSystem();
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        PluginRepositoryService repositoryService = pluginProxy.getPluginRepositoryService();
        PluginRepository selected = repository(1L);
        PluginRepository next = repository(2L);
        when(repositoryService.findAll()).thenReturn(List.of(selected, next));
        when(repositoryService.getSelectedRepository()).thenReturn(selected);
        when(repositoryService.toAppRepository(selected)).thenReturn(AppRepository.pluginRepo());
        when(repositoryService.toAppRepository(next)).thenReturn(AppRepository.pluginRepo());
        org.mockito.Mockito.doThrow(new IllegalStateException("Repository unavailable"))
                .when(repositoryService).selectRepository(2L);

        MarketplaceView view = new MarketplaceView(pluginProxy);
        ComboBox<PluginRepository> selector = findSelector(view);

        selector.setValue(next);

        assertEquals(selected, selector.getValue());
    }

    @Test
    void whenUserHasNoManagementPermissionThenRepositorySelectionIsRejected() {
        VaadinMock.mockSystem(new UserDTO(3, "user", 0));
        PluginProxy pluginProxy = ProxyMock.pluginProxyMock();
        PluginRepositoryService repositoryService = pluginProxy.getPluginRepositoryService();
        PluginRepository selected = repository(1L);
        PluginRepository next = repository(2L);
        when(repositoryService.findAll()).thenReturn(List.of(selected, next));
        when(repositoryService.getSelectedRepository()).thenReturn(selected);
        when(repositoryService.toAppRepository(selected)).thenReturn(AppRepository.pluginRepo());

        MarketplaceView view = new MarketplaceView(pluginProxy);
        ComboBox<PluginRepository> selector = findSelector(view);

        assertFalse(selector.isEnabled());
        selector.setValue(next);

        verify(repositoryService, never()).selectRepository(2L);
        assertEquals(selected, selector.getValue());
    }

    private static PluginRepository repository(Long id) {
        PluginRepository repository = org.mockito.Mockito.mock(PluginRepository.class);
        when(repository.getId()).thenReturn(id);
        when(repository.getName()).thenReturn("Repository " + id);
        when(repository.getUrl()).thenReturn("https://repo" + id + ".example.com");
        return repository;
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<PluginRepository> findSelector(MarketplaceView view) {
        return (ComboBox<PluginRepository>) view.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .map(HorizontalLayout.class::cast)
                .flatMap(Component::getChildren)
                .filter(ComboBox.class::isInstance)
                .findFirst()
                .orElseThrow();
    }
}
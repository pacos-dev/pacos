package org.pacos.core.component.dock.view.dock;

import org.config.VaadinMock;
import org.config.util.TestWindow;
import org.config.util.TestWindowConfig;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
import org.pacos.core.component.settings.view.config.SettingsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.config.PluginManagerMock.mockPluginResources;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class OsDockTest {

    @Mock
    private DockServiceProxy dockServiceProxy;
    private TestWindowConfig windowConfig;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        MockitoAnnotations.openMocks(this);
        this.windowConfig = new TestWindowConfig();
    }

    @Test
    void whenInitializedThenDockContainsOnlyOneElementThatIsPinnedByTheUserToTheDock() {
        DockConfigurationDTO configurationDTO = new DockConfigurationDTO(1);
        configurationDTO.setActivator(TestWindow.class.getSimpleName());
        configurationDTO.setUserId(2);
        configurationDTO.setOrderNum(1);

        when(dockServiceProxy.loadConfigurations(2)).thenReturn(List.of(configurationDTO));
        mockPluginResources(Map.of("testWindow", windowConfig, "settings", new SettingsConfig()));

        OsDock osDock = assertDoesNotThrow(() -> new OsDock(dockServiceProxy));
        assertEquals(1, osDock.getDockElementMap().size());
        assertNotNull(osDock.getDockElementMap().get(windowConfig));
    }

    @Test
    void whenModuleIsOpenedThenCssClassIsAddedToTheExistingDockElement() {
        DockConfigurationDTO configurationDTO = new DockConfigurationDTO(1);
        configurationDTO.setActivator(TestWindow.class.getSimpleName());
        configurationDTO.setUserId(2);
        configurationDTO.setOrderNum(1);

        when(dockServiceProxy.loadConfigurations(2)).thenReturn(List.of(configurationDTO));
        mockPluginResources(Map.of("testWindow", windowConfig));
        OsDock dock = new OsDock(dockServiceProxy);
        //when
        TestWindow testWindow = new TestWindow(windowConfig);
        dock.activateDockElement(testWindow);
        //then
        assertTrue(dock.getDockElementMap().get(windowConfig).getElement().getClassList().contains("active"));
    }

    @Test
    void whenModuleIsOpenedThenNewElementIsAddedToDock() {
        when(dockServiceProxy.loadConfigurations(2)).thenReturn(Collections.emptyList());
        mockPluginResources(Map.of("testWindow", windowConfig));
        OsDock dock = new OsDock(dockServiceProxy);
        assertEquals(0, dock.getDockElementMap().size());

        //when
        TestWindow testWindow = new TestWindow(windowConfig);
        dock.activateDockElement(testWindow);
        //then
        assertNotNull(dock.getDockElementMap().get(windowConfig));
    }


    @Test
    void whenModuleOpenedEventWithNullValueThenDockIsNotModified() {
        when(dockServiceProxy.loadConfigurations(2)).thenReturn(Collections.emptyList());
        mockPluginResources(Map.of());
        OsDock dock = new OsDock(dockServiceProxy);
        assertEquals(0, dock.getDockElementMap().size());
        //when
        UserSession.getCurrent().getUISystem().notify(ModuleEvent.MODULE_OPENED, null);
        //then
        assertEquals(0, dock.getDockElementMap().size());
    }

    @Test
    void whenWindowIsClosedAndIsNotPartOfUserDockConfigThenRemoveElementFromDock() {
        when(dockServiceProxy.loadConfigurations(2)).thenReturn(Collections.emptyList());
        mockPluginResources(Map.of("testWindow", windowConfig));
        OsDock dock = new OsDock(dockServiceProxy);
        TestWindow testWindow = new TestWindow(windowConfig);
        dock.activateDockElement(testWindow);
        //when
        assertEquals(1, dock.getDockElementMap().size());
        dock.deactivateDockElement(testWindow);
        //THEN
        assertEquals(0, dock.getDockElementMap().size());
    }

    @Test
    void whenWindowIsClosedAndIsPartOfUserConfigThenRemoveActiveClass() {
        DockConfigurationDTO configurationDTO = new DockConfigurationDTO(1);
        configurationDTO.setActivator(TestWindow.class.getSimpleName());
        configurationDTO.setUserId(2);
        configurationDTO.setOrderNum(1);

        when(dockServiceProxy.loadConfigurations(2)).thenReturn(List.of(configurationDTO));
        mockPluginResources(Map.of("testWindow", windowConfig));

        OsDock dock = new OsDock(dockServiceProxy);
        assertEquals(1, dock.getDockElementMap().size());

        TestWindow testWindow = new TestWindow(windowConfig);
        dock.activateDockElement(testWindow);
        //when
        assertEquals(1, dock.getDockElementMap().size());
        dock.deactivateDockElement(testWindow);
        //THEN
        assertEquals(1, dock.getDockElementMap().size());
        assertFalse(dock.getDockElementMap().get(windowConfig).getElement().getClassList().contains("active"));
    }

}
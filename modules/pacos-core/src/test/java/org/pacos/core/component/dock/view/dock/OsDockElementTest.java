package org.pacos.core.component.dock.view.dock;

import com.vaadin.flow.server.VaadinSession;
import org.config.VaadinMock;
import org.config.util.TestWindow;
import org.config.util.TestWindowConfig;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OsDockElementTest {

    @Mock
    private DockServiceProxy dockServiceProxy;
    @Mock
    private OsDock osDock;
    private TestWindowConfig windowConfig;
    private DockConfigurationDTO configurationDTO;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        assertNotNull(VaadinSession.getCurrent());
        assertNotNull(UISystem.getCurrent());
        assertNotNull(UserSession.getCurrent());
        assertNotNull(UISystem.getCurrent().getWindowManager());
        MockitoAnnotations.openMocks(this);
        this.windowConfig = spy(new TestWindowConfig());
        this.configurationDTO = new DockConfigurationDTO(1);
        this.configurationDTO.setActivator(TestWindow.class.getSimpleName());
        this.configurationDTO.setOrderNum(1);
    }

    @Test
    void whenStoreActivatorThenCallRepositoryMethod() {
        //given
        OsDockElement element = new OsDockElement(osDock, windowConfig, configurationDTO, dockServiceProxy);
        //when
        element.storeActivator(true);
        //then
        verify(dockServiceProxy).addActivator(windowConfig.activatorClass().getSimpleName(), UserSession.getCurrent().getUserId());
    }

    @Test
    void whenRemoveActivatorThenCallRepositoryMethod() {
        //given
        OsDockElement element = new OsDockElement(osDock, windowConfig, configurationDTO, dockServiceProxy);
        //when
        element.storeActivator(false);
        //then
        verify(dockServiceProxy).removeActivator(windowConfig.activatorClass().getSimpleName(), UserSession.getCurrent().getUserId());
    }

    @Test
    void whenIsActiveThenCallWindowManager() {
        //given
        OsDockElement element = new OsDockElement(osDock, windowConfig, configurationDTO, dockServiceProxy);
        when(UISystem.getCurrent().getWindowManager().isActive(windowConfig)).thenReturn(true);
        //then
        assertTrue(element.isActive());
    }

    @Test
    void whenCloseThenCloseAllActiveInstance() {
        //given
        OsDockElement element = new OsDockElement(osDock, windowConfig, configurationDTO, dockServiceProxy);
        TestWindow testWindow = new TestWindow(windowConfig);
        when(UISystem.getCurrent().getWindowManager().getInitializedWindows(windowConfig)).thenReturn(List.of(testWindow));
        AtomicInteger closedInstances = new AtomicInteger(0);
        UISystem.getCurrent().subscribe(ModuleEvent.MODULE_SHUTDOWN, e -> closedInstances.incrementAndGet());
        //when
        element.close();
        //then
        assertEquals(1, closedInstances.get());
    }

    @Test
    void whenCloseAndNoActiveInstanceThenDoNotSendNotification() {
        //given
        OsDockElement element = new OsDockElement(osDock, windowConfig, configurationDTO, dockServiceProxy);
        when(UISystem.getCurrent().getWindowManager().getInitializedWindows(windowConfig)).thenReturn(List.of());
        AtomicInteger closedInstances = new AtomicInteger(0);
        UISystem.getCurrent().subscribe(ModuleEvent.MODULE_SHUTDOWN, e -> closedInstances.incrementAndGet());
        //when
        element.close();
        //then
        assertEquals(0, closedInstances.get());
    }

    @Test
    void whenUserClickElementThenInitializeWindow() {
        //given
        OsDockElement element = new OsDockElement(osDock, windowConfig, configurationDTO, dockServiceProxy);
        when(UISystem.getCurrent().getWindowManager().getInitializedWindows(windowConfig)).thenReturn(List.of());
        //when
        element.openOrHideWindowOnUserClickEvent();
        //then
        verify(UISystem.getCurrent().getWindowManager()).showWindow(windowConfig.getClass());
    }

    @Test
    void whenWindowIsNotApplicationAndIsVisibleThenHide() {
        //given
        doReturn(false).when(windowConfig).isApplication();
        OsDockElement element = new OsDockElement(osDock, windowConfig, configurationDTO, dockServiceProxy);
        TestWindow testWindow = new TestWindow(windowConfig);
        element.setActiveInstance(testWindow);
        //when
        element.openOrHideWindowOnUserClickEvent();
        //then
        verify(UISystem.getCurrent().getWindowManager()).showOrHideAlreadyCreatedWindow(testWindow, false);
    }
}
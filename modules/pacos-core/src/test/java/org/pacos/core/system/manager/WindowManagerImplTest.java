package org.pacos.core.system.manager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.config.PluginManagerMock;
import org.config.VaadinMock;
import org.config.util.TestWindow;
import org.config.util.TestWindowConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.config.impl.WarningWindowConfig;
import org.pacos.base.window.manager.WindowInitializingException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WindowManagerImplTest {

    private WindowManagerImpl windowManager;
    private ApplicationContext mockContext;
    private TestWindow testWindow;
    private ModalWindowConfig modalConfig;
    private WarningWindowConfig warningConfig;

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
        TestWindowConfig testWindowConfig = new TestWindowConfig();
        mockContext = mock(ApplicationContext.class);
        AutowireCapableBeanFactory factoryMock = mock(AutowireCapableBeanFactory.class);
        when(mockContext.getAutowireCapableBeanFactory()).thenReturn(factoryMock);
        when(factoryMock.getBean(TestWindowConfig.class)).thenReturn(testWindowConfig);
        windowManager = new WindowManagerImpl();

        testWindow = spy(new TestWindow(testWindowConfig));
        modalConfig = new ModalWindowConfig();
        modalConfig.setContent(new VerticalLayout());
        warningConfig = new WarningWindowConfig("test", "test");


        when(mockContext.getAutowireCapableBeanFactory().getBean(WarningWindowConfig.class)).thenReturn(warningConfig);
        when(mockContext.getAutowireCapableBeanFactory().getBean(ModalWindowConfig.class)).thenReturn(modalConfig);
        when(mockContext.getAutowireCapableBeanFactory().getBean(TestWindowConfig.class)).thenReturn(testWindowConfig);
        when(mockContext.getAutowireCapableBeanFactory().createBean(TestWindow.class)).thenReturn(testWindow);
        PluginManagerMock.mockPluginResources(Map.of("testWindow", testWindowConfig, "modalConfig", modalConfig, "warningConfig", warningConfig), mockContext);
    }

    @Test
    void whenGetAllInitializedWindowThenReturnCollectionWithThreeWindow() {
        windowManager.showWindow(TestWindowConfig.class);
        windowManager.showModalWindow(modalConfig);
        windowManager.showWarningWindow(warningConfig);
        //when
        List<DesktopWindow> initializedWindows = windowManager.getAllInitializedWindows();
        //then
        assertEquals(3, initializedWindows.size());
    }

    @Test
    void whenWindowIsShutDownThenIsNotAPartOfInitializedWindows() {
        windowManager.showWindow(TestWindowConfig.class);
        windowManager.showModalWindow(modalConfig);
        DesktopWindow warningWindow = windowManager.showWarningWindow(warningConfig);

        windowManager.close(warningWindow);
        //when
        List<DesktopWindow> initializedWindows = windowManager.getAllInitializedWindows();
        //then
        assertEquals(2, initializedWindows.size());
    }

    @Test
    void whenWindowCantBeClosedThenIsStillInInitializedWindowsList() {
        DesktopWindow dw = windowManager.showWindow(TestWindowConfig.class);
        windowManager.showModalWindow(modalConfig);
        windowManager.showWarningWindow(warningConfig);
        doThrow(RuntimeException.class).when(dw).close();

        windowManager.close(dw);
        //when
        List<DesktopWindow> initializedWindows = windowManager.getAllInitializedWindows();
        //then
        assertEquals(3, initializedWindows.size());
    }

    @Test
    void whenWindowIsInitializedThenIsActive() {
        DesktopWindow initializedWindow = windowManager.showWindow(TestWindowConfig.class);
        //when
        assertTrue(windowManager.isActive(initializedWindow.getConfig()));
    }

    @Test
    void whenWindowIsNotInitializedThenIsNotActive() {
        assertFalse(windowManager.isActive(testWindow.getConfig()));
    }

    @Test
    void whenInitOrShowWindowThenModalWindowShouldBeCreated() {
        when(mockContext.getAutowireCapableBeanFactory().getBean(ModalWindowConfig.class)).thenReturn(modalConfig);

        DesktopWindow window = windowManager.showWindow(ModalWindowConfig.class);

        assertNotNull(window);
        verify(mockContext.getAutowireCapableBeanFactory(), times(1)).getBean(ModalWindowConfig.class);
    }

    @Test
    void whenInitOrShowWarningWindowThenModalWindowShouldBeCreated() {
        when(mockContext.getAutowireCapableBeanFactory().getBean(WarningWindowConfig.class)).thenReturn(warningConfig);

        DesktopWindow window = windowManager.showWindow(WarningWindowConfig.class);

        assertNotNull(window);
        verify(mockContext.getAutowireCapableBeanFactory(), times(1)).getBean(WarningWindowConfig.class);
    }

    @Test
    void whenErrorWhileCreatingWindowThenThrowException() {
        when(mockContext.getAutowireCapableBeanFactory().getBean(WarningWindowConfig.class)).thenThrow(BeanInitializationException.class);

        assertThrows(WindowInitializingException.class, () -> windowManager.showWindow(WarningWindowConfig.class));
    }

    @Test
    void whenShutDownWindowThenWindowShouldBeClosed() {
        when(testWindow.getConfig()).thenReturn(modalConfig);
        windowManager.showModalWindow(modalConfig);
        windowManager.close(testWindow);

        verify(testWindow, times(1)).close();
    }

    @Test
    void whenShowAndMoveToFrontThenWindowShouldBeOnFront() {
        windowManager.showAndMoveToFront(testWindow);

        verify(testWindow, times(1)).moveToFront();
    }

    @Test
    void whenDetachAllThenAllWindowsShouldBeClosed() {
        DesktopWindow tw = windowManager.showWindow(TestWindowConfig.class);

        windowManager.detachAll();

        assertTrue(((TestWindow) tw).isWasClosed());
    }

    @Test
    void whenGetInitializedWindowsThenReturnCorrectWindows() {
        windowManager.showModalWindow(modalConfig);

        List<DesktopWindow> windows = windowManager.getInitializedWindows(modalConfig);

        assertNotNull(windows);
        assertEquals(1, windows.size());
    }

    @Test
    void whenGetWindowDisplayedOnFrontThenReturnTopmostWindow() {
        DesktopWindow desktopWindow = windowManager.showModalWindow(modalConfig);

        Optional<DesktopWindow> frontWindow = windowManager.getWindowDisplayedOnFront();

        assertTrue(frontWindow.isPresent());
        assertEquals(desktopWindow, frontWindow.get());
    }

    @Test
    void whenGetWindowDisplayedOnFrontThenReturnEmptyResult() {
        Optional<DesktopWindow> frontWindow = windowManager.getWindowDisplayedOnFront();

        assertFalse(frontWindow.isPresent());
    }

    @Test
    void whenWindowIsInitializedThenMinimize() {
        DesktopWindow de = windowManager.showWindow(TestWindowConfig.class);
        //when
        windowManager.showOrHideAlreadyCreatedWindow(de, false);
        //then
        verify(de).moveToFront(); //while showWindow
        verify(de).minimize();
    }

    @Test
    void whenWindowIsBehindThenMoveToFront() {
        DesktopWindow de = windowManager.showWindow(TestWindowConfig.class);
        DesktopWindow modal = windowManager.showModalWindow(modalConfig);
        //simulate z-index chang called by frontend
        modal.markWindowOnTop();
        //when
        windowManager.showOrHideAlreadyCreatedWindow(de, false);
        //then
        verify(de, times(2)).moveToFront();
    }

    @Test
    void whenShowTheSameWindowAgainAndMultipleInstanceIsNotAllowedThenMinimize() {
        DesktopWindow de = windowManager.showWindow(TestWindowConfig.class);
        //when
        windowManager.showWindow(TestWindowConfig.class);
        //then
        verify(de, times(1)).minimize();
    }

    @Test
    void whenShowTheSameWindowAgainAndWindowIsNotAllowedToMinimizeThenMoveToFront() {
        modalConfig.setAllowMultipleInstance(false);
        windowManager = spy(windowManager);
        DesktopWindow de = spy(windowManager.showModalWindow(modalConfig));
        doReturn(Optional.of(de)).when(windowManager).getActiveWindow(modalConfig);
        //when
        windowManager.showModalWindow(modalConfig);
        //then
        verify(de).moveToFront();
    }

    @Test
    void whenGetWindowOnFrontTheReturnLatestMarkedByClient(){
        DesktopWindow de = windowManager.showWindow(TestWindowConfig.class);
        DesktopWindow de2 = windowManager.showWindow(WarningWindowConfig.class);
        //then
        Optional<DesktopWindow> window = windowManager.getWindowDisplayedOnFront();
        assertTrue(window.isPresent());
        assertEquals(de2, window.get());
        //when switch by client
        windowManager.markWindowOnTop(de);
        //then
        window = windowManager.getWindowDisplayedOnFront();
        assertTrue(window.isPresent());
        assertEquals(de, window.get());
    }

}

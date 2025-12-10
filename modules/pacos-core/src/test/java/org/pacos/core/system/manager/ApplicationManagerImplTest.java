package org.pacos.core.system.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.config.PluginManagerMock;
import org.config.VaadinMock;
import org.config.util.TestWindowConfig;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.file.FileInfo;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.FileOpenAllowed;
import org.pacos.base.window.config.FileExtensionHandler;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.base.window.manager.WindowManager;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApplicationManagerImplTest {

    @Test
    void whenModuleIsAllowedToOpenFileThenOpenMethodIsTriggeredOnModule() {
        VaadinMock.mockSystem();
        ApplicationContext context = mock(ApplicationContext.class);
        AtomicInteger counter = new AtomicInteger();
        Map<String, WindowConfig> configMap = new HashMap<>();
        configMap.put("testWindow", new TestWindowConfig());
        configMap.put("testWindowManager", new TestWindowConfigForAppManager());
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(configMap);
        PluginManagerMock.mockPluginResources(configMap);
        FileInfo fileInfo = mock(FileInfo.class);
        when(fileInfo.getExtension()).thenReturn("TXT");
        WindowManager windowManager = UISystem.getCurrent().getWindowManager();
        doReturn(new TestWindowForManager(new TestWindowConfig(), counter)).when(windowManager)
                .showWindow(TestWindowConfigForAppManager.class);
        //when
        ApplicationManagerImpl manager = new ApplicationManagerImpl();
        manager.open(fileInfo);
        //then
        assertEquals(1, counter.get());
    }

    @Test
    void whenNoAvailableModuleToOpenFileThenNoInteractionOnUI() {
        VaadinMock.mockSystem();
        ApplicationContext context = mock(ApplicationContext.class);
        AtomicInteger counter = new AtomicInteger();
        Map<String, WindowConfig> configMap = new HashMap<>();
        configMap.put("testWindow", new TestWindowConfig());
        configMap.put("testWindowManager", new TestWindowConfigForAppManager());
        when(context.getBeansOfType(WindowConfig.class)).thenReturn(configMap);

        FileInfo fileInfo = mock(FileInfo.class);
        when(fileInfo.getExtension()).thenReturn("exe");
        //when
        ApplicationManagerImpl manager = new ApplicationManagerImpl();
        manager.open(fileInfo);
        //then
        assertEquals(0, counter.get());
        verifyNoInteractions(UISystem.getCurrent().getWindowManager());
    }


    static class TestWindowConfigForAppManager implements WindowConfig, FileExtensionHandler {

        @Override
        public String title() {
            return "test";
        }

        @Override
        public String icon() {
            return "test";
        }

        @Override
        public Class<? extends DesktopWindow> activatorClass() {
            return TestWindowForManager.class;
        }

        @Override
        public boolean isApplication() {
            return true;
        }

        @Override
        public boolean isAllowMultipleInstance() {
            return false;
        }

        @Override
        public List<String> allowedExtension() {
            return List.of("txt");
        }
    }

    static class TestWindowForManager extends DesktopWindow implements FileOpenAllowed {

        private final AtomicInteger atomicInteger;

        public TestWindowForManager(WindowConfig moduleConfig, AtomicInteger atomicInteger) {
            super(moduleConfig);
            this.atomicInteger = atomicInteger;
        }

        @Override
        public void openFile(FileInfo fileInfo) {
            atomicInteger.incrementAndGet();

        }
    }
}

package org.pacos.core.component.dock.view.dock;

import com.vaadin.flow.server.VaadinSession;
import org.config.VaadinMock;
import org.config.util.TestWindow;
import org.config.util.TestWindowConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.proxy.DockServiceProxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;

class OsDockElementContextMenuTest {

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
        this.configurationDTO.setUserId(1);
        this.configurationDTO.setOrderNum(1);
    }

    @Test
    void whenInitializedThenNoException() {
        OsDockElement element = new OsDockElement(osDock, windowConfig, configurationDTO, dockServiceProxy);
        assertDoesNotThrow(() -> OsDockElementContextMenu.configureFor(element, new TestWindowConfig()));
    }

}
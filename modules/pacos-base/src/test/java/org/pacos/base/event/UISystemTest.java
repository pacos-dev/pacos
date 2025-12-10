package org.pacos.base.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pacos.base.mock.VaadinMock;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.ApplicationManager;
import org.pacos.base.window.manager.DownloadManager;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.base.window.manager.VariableManager;
import org.pacos.base.window.manager.WindowManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UISystemTest {

    @InjectMocks
    private UISystem uiSystem;

    @Mock
    private DownloadManager downloadManager;
    @Mock
    private VariableManager variableManager;
    @Mock
    private WindowManager windowManager;
    @Mock
    private ApplicationManager applicationManager;
    @Mock
    private ShortcutManager shortcutManager;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenGetWindowManagerThenReturnConfiguredObject() {
        assertEquals(windowManager, uiSystem.getWindowManager());
    }

    @Test
    void whenGetVariableModalThenReturnConfiguredObject() {
        assertEquals(variableManager, uiSystem.getVariableManager());
    }


    @Test
    void whenGetApplicationManagerThenReturnConfiguredObject() {
        assertEquals(applicationManager, uiSystem.getApplicationManager());
    }

    @Test
    void whenGetShortcutManagerThenReturnConfiguredObject() {
        assertEquals(shortcutManager, uiSystem.getShortcutManager());
    }

    @Test
    void whenGetCurrentThenReturnObjectFromSession() {
        VaadinMock.mockSystem();
        UserSession.getCurrent().setUiSystem(uiSystem);
        assertEquals(uiSystem, UISystem.getCurrent());
    }

    @Test
    void whenGetDownloadManagerThenReturnConfiguredObject() {
        assertEquals(downloadManager, uiSystem.getDownloadManager());
    }

}
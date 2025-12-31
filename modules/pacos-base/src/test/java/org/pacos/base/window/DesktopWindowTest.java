package org.pacos.base.window;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.event.UISystem;
import org.pacos.base.mock.VaadinMock;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.base.window.shortcut.ShortcutType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DesktopWindowTest {

    private DesktopWindowTestImpl window;
    private WindowConfigTest config;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        assertNotNull(UserSession.getCurrent());
        assertNotNull(UISystem.getCurrent());
        this.config = new WindowConfigTest();
        this.window = spy(new DesktopWindowTestImpl(config));
    }

    @Test
    void whenInitializeSuperConstructorThenNoException() {
        assertNotNull(UserSession.getCurrent());
        assertDoesNotThrow(() -> new DesktopWindowTestImpl(config));
    }

    @Test
    void whenRegisterShortcutThenCallShortCutManager() {

        ShortcutEventListener shortcutEventListener = e -> {
        };
        window.registerShortcut(ShortcutType.CUT, shortcutEventListener);
        //then
        ShortcutManager manager = UserSession.getCurrent().getUISystem().getShortcutManager();
        verify(manager).registerShortcut(window, ShortcutType.CUT.getShortcut(), shortcutEventListener);
    }

    @Test
    void whenCloseThenUnregisterAllShortcuts() {
        //when
        window.close();
        //then
        verify(UISystem.getCurrent().getShortcutManager()).unregisterAll(window);
    }

    @Test
    void whenAddThenAddComponentToContent() {
        //given
        Component comp = new VerticalLayout();
        //when
        window.add(comp);
        //then
        assertEquals(comp, window.getContent().getComponentAt(0));
    }

    @Test
    void whenSetSizeThenWindowSizeIsChanged() {
        //given
        window.setSize(100, 200);
        //then
        assertEquals("100.0px", window.getWidth());
        assertEquals("200.0px", window.getHeight());
    }


    @Test
    void whenGetConfigThenReturnPassedObject() {
        assertEquals(config, window.getConfig());
    }


    @Test
    void whenMoveToFrontThenCallJsScript() {
        try (MockedStatic<DialogJS> dialogJS = mockStatic(DialogJS.class)) {
            dialogJS.when(() -> DialogJS.moveToFront(any()))
                    .thenAnswer(inv -> null);
            //when
            window.moveToFront();
            //then
            dialogJS.verify(() -> DialogJS.moveToFront(window));
        }
    }

    @Test
    void whenClientCallbackForHeaderDblClickThenExpandWindow() {
        //when
        window.onHeaderDoubleClick();
        //then
        assertTrue(window.getExpandInfo().isExpanded());
    }

    @Test
    void whenClientCallbackForHeaderDblClickThenRestoreWindow() {
        //when
        window.onHeaderDoubleClick();
        window.onHeaderDoubleClick();
        //then
        assertFalse(window.getExpandInfo().isExpanded());
    }

    @Test
    void whenGetWindowHeaderThenReturnInitializedObject() {
        assertNotNull(window.getWindowHeader());
    }

    @Test
    void whenGetExpandInfoThenReturnInitializedObject() {
        assertNotNull(window.getExpandInfo());
    }

    @Test
    void wheSetCloseOnOutsideToTrueThenPropertyIsSet() {
        window.allowCloseOnOutsideClick();
        //then
        assertTrue(window.isCloseOnOutsideClick());
    }

    @Test
    void whenInitializedWindowThenIsCloseOnOutsideClickIsFalse() {
        assertFalse(window.isCloseOnOutsideClick());
    }

    @Test
    void whenAllowCloseOnEscThenPropertyIsSet() {
        window.allowCloseOnEsc();
        //then
        assertTrue(window.isCloseOnEsc());
    }

    @Test
    void whenInitializedWindowThenIsCloseOnEscIsFalse() {
        assertFalse(window.isCloseOnEsc());
    }

    @Test
    void whenToStringThenFormatOutput() {
        assertEquals("DesktopWindow{\"Test window\"}", window.toString());
    }

    @Test
    void whenWithConfirmationFooterBtnThenAddComponentToWindowFooter() {
        //when
        window.withConfirmationFooterBtn(() -> true);
        //then
        assertEquals(1, window.getFooter().getElement().getChildren().count());
    }

    @Test
    void whenWithCancelFooterBtnThenAddComponentToWindowFooter() {
        //when
        window.withCancelFooterBtn();
        //then
        assertEquals(1, window.getFooter().getElement().getChildren().count());
    }

    @Test
    void whenAddWindowToMasterWindowThenChildListContainsWindow() {
        //given
        ModalWindowConfig config = new ModalWindowConfig();
        ModalWindow childModal = new ModalWindow(config);
        when(UISystem.getCurrent().getWindowManager().showModalWindow(config))
                .thenReturn(childModal);
        //when
        window.addWindow(config);
        //then
        assertTrue(window.getChildWindow().contains(childModal));
        assertEquals(childModal.getParentWindow(), window);
    }

    static class WindowConfigTest implements WindowConfig {

        @Override
        public String title() {
            return "Test window";
        }

        @Override
        public String icon() {
            return "icon";
        }

        @Override
        public Class<? extends DesktopWindow> activatorClass() {
            return null;
        }

        @Override
        public boolean isApplication() {
            return false;
        }

        @Override
        public boolean isAllowMultipleInstance() {
            return false;
        }
    }

    static class DesktopWindowTestImpl extends DesktopWindow {
        protected DesktopWindowTestImpl(WindowConfigTest config) {
            super(config);
        }
    }
}
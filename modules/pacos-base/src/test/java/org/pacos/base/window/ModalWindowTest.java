package org.pacos.base.window;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.Theme;
import org.pacos.base.mock.VaadinMock;
import org.pacos.base.window.config.impl.ModalWindowConfig;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

class ModalWindowTest {

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
    }

    @Test
    void whenCreateWindowThenContentIsTakenFromConfiguration() {
        ModalWindowConfig windowConfig = new ModalWindowConfig();
        windowConfig.setWarning(false);
        windowConfig.setContent(new VerticalLayout());
        //when
        ModalWindow modalWindow = new ModalWindow(windowConfig);
        //then
        assertEquals(windowConfig.getContent(), modalWindow.getContent().getChildren().findFirst().get());
    }

    @Test
    void whenCreateWarningWindowThenThemIsAdded() {
        ModalWindowConfig windowConfig = new ModalWindowConfig();
        windowConfig.setContent(new VerticalLayout());
        windowConfig.setWarning(true);
        //when
        ModalWindow modalWindow = new ModalWindow(windowConfig);
        //then
        assertTrue(modalWindow.getThemeName().contains(Theme.BLACK_WINDOW.getName()));
    }
}
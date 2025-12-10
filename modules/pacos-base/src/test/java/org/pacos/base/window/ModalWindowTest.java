package org.pacos.base.window;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.mock.VaadinMock;
import org.pacos.base.window.config.impl.ModalWindowConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModalWindowTest {

    @BeforeEach
    void init(){
        VaadinMock.mockSystem();
    }
    @Test
    void whenCreateWindowThenContentIsTakenFromConfiguration(){
        ModalWindowConfig windowConfig = new ModalWindowConfig();
        windowConfig.setContent(new VerticalLayout());
        //when
        ModalWindow modalWindow = new ModalWindow(windowConfig);
        //then
        assertEquals(windowConfig.getContent(),modalWindow.getContent());
    }
}
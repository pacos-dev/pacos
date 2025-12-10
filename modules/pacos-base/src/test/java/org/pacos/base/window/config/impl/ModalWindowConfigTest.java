package org.pacos.base.window.config.impl;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.junit.jupiter.api.Test;
import org.pacos.base.window.ModalWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModalWindowConfigTest {

    @Test
    void whenInitializeModalWindowConfigThenLayoutIsNotNull() {
        assertNotNull(new ModalWindowConfig().getContent());
    }

    @Test
    void whenSetContentThenContentIsSet() {
        ModalWindowConfig config = new ModalWindowConfig();
        VerticalLayout layout = new VerticalLayout();
        config.setContent(layout);
        //then
        assertEquals(layout, config.getContent());
    }

    @Test
    void whenSetTitleThenTitleIsSet() {
        ModalWindowConfig config = new ModalWindowConfig();
        config.setTitle("My title");
        //then
        assertEquals("My title", config.title());
    }

    @Test
    void whenSetIconThenIconIsSet() {
        ModalWindowConfig config = new ModalWindowConfig();
        config.setIcon("My icon");
        //then
        assertEquals("My icon", config.icon());
    }

    @Test
    void whenMultipleInstanceThenTrue() {
        ModalWindowConfig config = new ModalWindowConfig();
        config.setAllowMultipleInstance(true);
        //then
        assertTrue(config.isAllowMultipleInstance());
    }

    @Test
    void whenIsAllowedForMinimizeThenFalse() {
        ModalWindowConfig config = new ModalWindowConfig();
        //then
        assertFalse(config.isAllowedForMinimize());
    }

    @Test
    void whenIsApplicationThenFalse() {
        ModalWindowConfig config = new ModalWindowConfig();
        //then
        assertFalse(config.isApplication());
    }

    @Test
    void whenGetWindowStateThenReturnConfiguration(){
        ModalWindowConfig config = new ModalWindowConfig();
        //then
        assertNotNull(config.getWindowState());
    }
    @Test
    void whenIsAllowedForCurrentSessionThenReturnTrue(){
        ModalWindowConfig config = new ModalWindowConfig();
        //then
        assertTrue(config.isAllowedForCurrentSession(null));
    }

    @Test
    void whenActivatorClass(){
        ModalWindowConfig config = new ModalWindowConfig();
        assertEquals(ModalWindow.class,config.activatorClass());
    }

}
package org.pacos.base.window;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.pacos.base.window.event.OnConfirmEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowStateTest {

    @Test
    void whenModalThenSetsModalStateCorrectly() {
        WindowState state = new WindowState().modal();
        assertTrue(state.draggable);
        assertFalse(state.resizable);
        assertTrue(state.modal);
        assertFalse(state.minimizeAllowed);
        assertTrue(state.cancellable);
    }

    @Test
    void whenWithDraggableTrueThenSetsDraggableToTrue() {
        WindowState state = new WindowState().withDraggable(true);
        assertTrue(state.draggable);
    }

    @Test
    void whenWithDraggableFalseThenSetsDraggableToFalse() {
        WindowState state = new WindowState().withDraggable(false);
        assertFalse(state.draggable);
    }

    @Test
    void whenWithResizableTrueThenSetsResizableToTrue() {
        WindowState state = new WindowState().withResizable(true);
        assertTrue(state.resizable);
    }

    @Test
    void whenWithResizableFalseThenSetsResizableToFalse() {
        WindowState state = new WindowState().withResizable(false);
        assertFalse(state.resizable);
    }

    @Test
    void whenWithModalTrueThenSetsModalToTrue() {
        WindowState state = new WindowState().withModal(true);
        assertTrue(state.modal);
    }

    @Test
    void whenWithModalFalseThenSetsModalToFalse() {
        WindowState state = new WindowState().withModal(false);
        assertFalse(state.modal);
    }

    @Test
    void whenWithMinimizeAllowedTrueThenSetsMinimizeAllowedToTrue() {
        WindowState state = new WindowState().withMinimizeAllowed(true);
        assertTrue(state.minimizeAllowed);
    }

    @Test
    void whenWithMinimizeAllowedFalseThenSetsMinimizeAllowedToFalse() {
        WindowState state = new WindowState().withMinimizeAllowed(false);
        assertFalse(state.minimizeAllowed);
    }

    @Test
    void whenWithWidthThenSetsWidthCorrectly() {
        WindowState state = new WindowState().withWidth(500);
        assertEquals(500, state.width);
    }

    @Test
    void whenWithHeightThenSetsHeightCorrectly() {
        WindowState state = new WindowState().withHeight(300);
        assertEquals(300, state.height);
    }

    @Test
    void whenSetCancellableTrueThenSetsCancellableToTrue() {
        WindowState state = new WindowState().setCancellable(true);
        assertTrue(state.cancellable);
    }

    @Test
    void whenSetCancellableFalseThenSetsCancellableToFalse() {
        WindowState state = new WindowState().setCancellable(false);
        assertFalse(state.cancellable);
    }

    @Test
    void whenWithConfirmEventThenSetsConfirmEventCorrectly() {
        OnConfirmEvent event = () -> true;
        WindowState state = new WindowState().withConfirmEvent(event);
        assertEquals(event, state.confirmEvent);
    }

    @Test
    void whenAddFooterComponentThenAddsComponentToFooter() {
        Component component = new TextField();
        WindowState state = new WindowState();
        state.addFooterComponent(component);
        assertTrue(state.footerComponent.contains(component));
    }
}

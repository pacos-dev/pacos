package org.pacos.core.component.menu;

import java.util.Map;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import org.config.PluginManagerMock;
import org.config.VaadinMock;
import org.config.util.TestWindowConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.DesktopWindow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApplicationsModalTest {

    private ApplicationsModal modal;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        PluginManagerMock.mockPluginResources(Map.of("testWindow", new TestWindowConfig()));
        this.modal = new ApplicationsModal();
        this.modal.open();
    }

    @Test
    void whenOpenApplicationModalThenResetFilters() {
        //give
        this.modal.searchField.setValue("test");
        //when
        this.modal.open();
        //then
        assertEquals("", this.modal.searchField.getValue());
    }

    @Test
    void whenOpenApplicationModalThenMoveToFront() {
        //give
        this.modal.searchField.setValue("test");
        //when
        assertDoesNotThrow(()->this.modal.open());
        //then
    }

    @Test
    void whenUserTypeEnterKeyAndFilterNotMatchAnyConfigThenDoNotRunAnyApplication() {
        KeyDownEvent event = mock(KeyDownEvent.class);
        when(event.getKey()).thenReturn(Key.ENTER);
        this.modal.searchField.setValue("glogg");
        //when
        this.modal.onKeyDownEvent(event);
        //then
        verifyNoInteractions(UISystem.getCurrent().getWindowManager());
    }

    @Test
    void whenUserTypeEnterKeyThenRunFirstApplicationOnTheList() {
        KeyDownEvent event = mock(KeyDownEvent.class);
        when(event.getKey()).thenReturn(Key.ENTER);
        DesktopWindow window = mock(DesktopWindow.class);
        when(UISystem.getCurrent().getWindowManager().showWindow(TestWindowConfig.class)).thenReturn(window);
        //when
        this.modal.onKeyDownEvent(event);
        //then
        verify(window).open();
        verify(window).moveToFront();
    }
}
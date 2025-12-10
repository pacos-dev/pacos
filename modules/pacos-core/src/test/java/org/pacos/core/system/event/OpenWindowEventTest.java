package org.pacos.core.system.event;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.Test;
import org.pacos.base.window.DesktopWindow;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenWindowEventTest {

    @Test
    void whenForWindowIsCalledThenDesktopWindowIsOpenedAndMovedToFront() {
        DesktopWindow mockWindow = mock(DesktopWindow.class);
        when(mockWindow.getElement()).thenReturn(mock(Element.class));
        UI ui = mock(UI.class);

        OpenWindowEvent.fireEvent(mockWindow, ui);

        verify(mockWindow.getElement()).removeFromTree();
        verify(ui).add(mockWindow);
        verify(mockWindow).open();
        verify(mockWindow).moveToFront();

    }
}

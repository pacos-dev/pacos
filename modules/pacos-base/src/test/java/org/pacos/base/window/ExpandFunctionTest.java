package org.pacos.base.window;

import com.vaadin.flow.dom.Element;
import elemental.json.JsonArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpandFunctionTest {

    private DesktopWindow mockDesktopWindow;
    private ExpandFunction expandFunction;

    @BeforeEach
    void setUp() {
        mockDesktopWindow = mock(DesktopWindow.class);
        when(mockDesktopWindow.getElement()).thenReturn(mock(Element.class));
        when(mockDesktopWindow.getWidth()).thenReturn("800px");
        when(mockDesktopWindow.getHeight()).thenReturn("600px");
        expandFunction = Mockito.spy(new ExpandFunction(false, "800px", "600px", mockDesktopWindow));
    }

    @Test
    void whenModifyThenWidthAndHeightAreSetAndWindowIsNotDraggableOrResizable() {
        expandFunction.modify();

        verify(mockDesktopWindow).setWidth("800px");
        verify(mockDesktopWindow).setHeight("600px");
        verify(mockDesktopWindow).setDraggable(true);
        verify(mockDesktopWindow).setResizable(true);
    }

    @Test
    void whenWriteCurrentPositionThenAtTheEndResetWindowPosition() {
        JsonArray js = Mockito.mock(JsonArray.class);
        when(js.getString(0)).thenReturn("200px");
        when(js.getString(1)).thenReturn("300px");

        expandFunction.writeCurrentPosition(js);

        verify(mockDesktopWindow, times(1)).setPosition("0px", "0px");

        assertEquals("200px", expandFunction.getLeft());
        assertEquals("300px", expandFunction.getTop());
    }

    @Test
    void whenExpandThenIsExpanded() {
        JsonArray js = Mockito.mock(JsonArray.class);
        when(js.getString(0)).thenReturn("200px");
        when(js.getString(1)).thenReturn("300px");

        doNothing().when(expandFunction).readCurrentPosition();
        expandFunction.expand();
        expandFunction.writeCurrentPosition(js);
        //then

        verify(mockDesktopWindow, times(1)).setPosition("0px", "0px");

        assertTrue(expandFunction.isExpanded());
        verify(mockDesktopWindow).setWidth("100%");
        verify(mockDesktopWindow).setHeight("100%");

    }

    @Test
    void whenIsExpandAndExpandIsCalledThenRestorePosition() {
        JsonArray js = Mockito.mock(JsonArray.class);
        when(js.getString(0)).thenReturn("200px");
        when(js.getString(1)).thenReturn("300px");
        doNothing().when(expandFunction).readCurrentPosition();
        expandFunction.expand();
        expandFunction.writeCurrentPosition(js);
        //when
        expandFunction.expand();
        //then
        assertFalse(expandFunction.isExpanded());
        verify(mockDesktopWindow, times(1)).setPosition("200px", "300px");
        verify(mockDesktopWindow, times(1)).setWidth("800px");
        verify(mockDesktopWindow, times(1)).setHeight("600px");


    }

    @Test
    void whenSetLeftAndSetTopThenValuesAreUpdated() {
        expandFunction.setLeft("200px");
        expandFunction.setTop("300px");

        assertEquals("200px", expandFunction.getLeft());
        assertEquals("300px", expandFunction.getTop());
    }
}

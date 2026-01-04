package org.pacos.base.window;

import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.exception.PacosException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        expandFunction.writeCurrentPosition(List.of("200px","300px"));

        verify(mockDesktopWindow, times(1)).setPosition("0px", "0px");

        assertEquals("200px", expandFunction.getLeft());
        assertEquals("300px", expandFunction.getTop());
    }

    @Test
    void whenExpandThenIsExpanded(){
        doNothing().when(expandFunction).readCurrentPosition();
        expandFunction.expand();
        expandFunction.writeCurrentPosition(List.of("200px","300px"));
        //then

        verify(mockDesktopWindow, times(1)).setPosition("0px", "0px");

        assertTrue(expandFunction.isExpanded());
        verify(mockDesktopWindow).setWidth("100%");
        verify(mockDesktopWindow).setHeight("100%");
    }

    @Test
    void whenIsExpandAndExpandIsCalledThenRestorePosition(){
        doNothing().when(expandFunction).readCurrentPosition();
        expandFunction.expand();
        expandFunction.writeCurrentPosition(List.of("200px","300px"));
        //when
        expandFunction.expand();
        //then
        assertFalse(expandFunction.isExpanded());
        verify(mockDesktopWindow, times(1)).setPosition("200px", "300px");
        verify(mockDesktopWindow, times(1)).setWidth("800px");
        verify(mockDesktopWindow, times(1)).setHeight("600px");


    }

    @Test
    void whenParsingErrorThenThrowException() {
        List<?> list = new ArrayList<>();
        assertThrows(PacosException.class, ()->expandFunction.writeCurrentPosition(list));
    }
}

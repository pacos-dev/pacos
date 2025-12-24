package org.pacos.base.window;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.utils.ObjectMapperUtils;
import tools.jackson.databind.JsonNode;

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
        JsonNode js = ObjectMapperUtils.getMapper().readTree("[\"200px\", \"300px\"]");

        expandFunction.writeCurrentPosition(js.toString());

        verify(mockDesktopWindow, times(1)).setPosition("0px", "0px");

        assertEquals("200px", expandFunction.getLeft());
        assertEquals("300px", expandFunction.getTop());
    }

    @Test
    void whenExpandThenIsExpanded(){
        JsonNode js = ObjectMapperUtils.getMapper().readTree("[\"200px\", \"300px\"]");

        doNothing().when(expandFunction).readCurrentPosition();
        expandFunction.expand();
        expandFunction.writeCurrentPosition(js.toString());
        //then

        verify(mockDesktopWindow, times(1)).setPosition("0px", "0px");

        assertTrue(expandFunction.isExpanded());
        verify(mockDesktopWindow).setWidth("100%");
        verify(mockDesktopWindow).setHeight("100%");
    }

    @Test
    void whenIsExpandAndExpandIsCalledThenRestorePosition(){
        JsonNode js = ObjectMapperUtils.getMapper().readTree("[\"200px\", \"300px\"]");

        doNothing().when(expandFunction).readCurrentPosition();
        expandFunction.expand();
        expandFunction.writeCurrentPosition(js.toString());
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

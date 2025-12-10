package org.pacos.common.view.tab;

import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TabMonitoringTest {

    @Test
    void whenCreateTabMonitoringThenSetSpanAsLabel(){
        TabMonitoring monitoring = new TabMonitoring("test");

        assertTrue(monitoring.getChildren().findFirst().isPresent());
        assertEquals("test",((Span)monitoring.getChildren().findFirst().get()).getText());
    }

    @Test
    void whenUpdateLabelThenUpdateSpanContent(){
        TabMonitoring monitoring = new TabMonitoring("test");
        monitoring.updateLabel("test2");
        //then
        assertTrue(monitoring.getChildren().findFirst().isPresent());
        assertEquals("test2",((Span)monitoring.getChildren().findFirst().get()).getText());
    }

    @Test
    void whenMarkAsChangedThenSetStyleOnSpanLabel(){
        TabMonitoring monitoring = new TabMonitoring("test");
        monitoring.markChanges(true);
        //then
        assertTrue(monitoring.getChildren().findFirst().isPresent());
        assertEquals("changes",monitoring.getChildren().findFirst().get().getClassName());
    }

    @Test
    void whenMarkAsNoChangedThenRemoveStyle(){
        TabMonitoring monitoring = new TabMonitoring("test");
        monitoring.markChanges(true);
        //then
        monitoring.markChanges(false);
        assertTrue(monitoring.getChildren().findFirst().isPresent());
        assertNull(monitoring.getChildren().findFirst().get().getClassName());
    }

}
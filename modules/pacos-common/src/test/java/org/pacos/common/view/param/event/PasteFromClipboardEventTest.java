package org.pacos.common.view.param.event;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pacos.common.view.param.GridParam;
import org.pacos.common.view.param.Param;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PasteFromClipboardEventTest {

    @Test
    void whenPastedValueAreIncorrectThenNoChangesOnGrid() {
        GridParam gridParam = new GridParam(null);
        String param = "param:value";
        UI ui = mock(UI.class);
        //when
        PasteFromClipboardEvent.readClipboardAndModifyVariables(gridParam, CompletableFuture.completedFuture(param), ui);
        //then
        verifyNoInteractions(ui);
    }

    @Test
    void whenPasteVariableThenUpdateGrid() {
        GridParam gridParam = new GridParam(null);
        String param = "\"param\":\"value\"";
        UI ui = mock(UI.class);
        //when
        PasteFromClipboardEvent.readClipboardAndModifyVariables(gridParam, CompletableFuture.completedFuture(param), ui);
        //then
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(ui).access(captor.capture());
        //and when
        captor.getValue().execute();
        //then
        assertEquals(1, gridParam.getItems().size());
        assertEquals("param", gridParam.getItems().get(0).getName());
        assertEquals("value", gridParam.getItems().get(0).getValue());
        assertTrue(gridParam.getItems().get(0).isEnabled());
    }

    @Test
    void whenCantLoadClipboardValueThenNoException() {
        GridParam gridParam = new GridParam(List.of(new Param("key", "value", false)));
        UI ui = mock(UI.class);
        //when
        assertDoesNotThrow(() -> PasteFromClipboardEvent.readClipboardAndModifyVariables(gridParam, CompletableFuture.failedFuture(new RuntimeException()), ui));
        assertEquals(1, gridParam.getItems().size());
    }

    @Test
    void whenPasteVariablesThenCombineWithParamsFromGrid() {
        GridParam gridParam = new GridParam(List.of(new Param("key", "value", false)));
        String param = "{\"param\":\"value\",\"param2\":\"value2\"}";
        UI ui = mock(UI.class);
        //when
        PasteFromClipboardEvent.readClipboardAndModifyVariables(gridParam, CompletableFuture.completedFuture(param), ui);
        //then
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(ui).access(captor.capture());
        //and when
        captor.getValue().execute();
        //then
        assertEquals(3, gridParam.getItems().size());
    }

    @Test
    void whenPasteExistingVariablesThenDoNotAddDuplicates() {
        GridParam gridParam = new GridParam(List.of(new Param("key", "value", true)));
        String param = "{\"key\":\"value\"}";
        UI ui = mock(UI.class);
        //when
        PasteFromClipboardEvent.readClipboardAndModifyVariables(gridParam, CompletableFuture.completedFuture(param), ui);
        //then
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(ui).access(captor.capture());
        //and when
        captor.getValue().execute();
        //then
        assertEquals(1, gridParam.getItems().size());
        assertEquals("key", gridParam.getItems().get(0).getName());
        assertEquals("value", gridParam.getItems().get(0).getValue());
        assertTrue(gridParam.getItems().get(0).isEnabled());
    }


    @Test
    void whenPasteExistingVariablesThenEnableVariable() {
        GridParam gridParam = new GridParam(List.of(new Param("key", "value", false)));
        String param = "{\"key\":\"value\"}";
        UI ui = mock(UI.class);
        //when
        PasteFromClipboardEvent.readClipboardAndModifyVariables(gridParam, CompletableFuture.completedFuture(param), ui);
        //then
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(ui).access(captor.capture());
        //and when
        captor.getValue().execute();
        //then
        assertEquals(1, gridParam.getItems().size());
        assertEquals("key", gridParam.getItems().get(0).getName());
        assertEquals("value", gridParam.getItems().get(0).getValue());
        assertTrue(gridParam.getItems().get(0).isEnabled());
    }

    @Test
    void whenPasteExistingVariablesThenValueChanged() {
        GridParam gridParam = new GridParam(List.of(new Param("key", "value", false)));
        String param = "{\"key\":\"new value\"}";
        UI ui = mock(UI.class);
        //when
        PasteFromClipboardEvent.readClipboardAndModifyVariables(gridParam, CompletableFuture.completedFuture(param), ui);
        //then
        ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(ui).access(captor.capture());
        //and when
        captor.getValue().execute();
        //then
        assertEquals(1, gridParam.getItems().size());
        assertEquals("key", gridParam.getItems().get(0).getName());
        assertEquals("new value", gridParam.getItems().get(0).getValue());
        assertTrue(gridParam.getItems().get(0).isEnabled());
    }
}
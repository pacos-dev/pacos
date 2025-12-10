package org.pacos.common.view.param;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.ClipboardManager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GridParamTest {

    private static final VaadinSession session = mock(VaadinSession.class);

    @Test
    void whenInitializeWithNullCollectionThenNoExceptions() {
        assertDoesNotThrow(() -> new GridParam(null));
    }

    @Test
    void whenInitializeWithEmptyCollectionThenNoExceptions() {
        assertDoesNotThrow(() -> new GridParam(new ArrayList<>()));
    }


    @Test
    void whenGetItemsThenDoNotReturnEmptyLastElement() {
        GridParam grid = new GridParam(new ArrayList<>());
        assertTrue(grid.getItems().isEmpty());
    }

    @Test
    void whenInitializeRowThenBuildRemoveBtnForParam() {
        Param param = new Param("", "", true);
        Button btn = new Button("btn");
        //when
        new GridParam(new ArrayList<>()).buildDeletionButton(btn, param);
        //then
        assertTrue(btn.isVisible());
    }

    @Test
    void whenInitializeRowForLastParamThenBtnForParamIsNotVisible() {
        Param param = new Param("", "", true);
        Button btn = new Button("btn");
        //when
        GridParam gridParam = new GridParam(new ArrayList<>());
        gridParam.buildDeletionButton(btn, gridParam.last);
        //then
        assertFalse(btn.isVisible());
    }

    @Test
    void whenDeleteBtnClickedThenParamIsRemoved() {
        Param param = new Param("", "", true);
        GridParam gridParam = new GridParam(List.of(param));
        Button btn = new Button("btn");
        //when

        gridParam.buildDeletionButton(btn, param);
        btn.click();
        //then
        assertTrue(gridParam.getItems().isEmpty());
    }

    @Test
    void whenAddListenerThenIsCalledWhenValueIsChanges() {
        AtomicInteger counter = new AtomicInteger();
        GridParam grid = new GridParam(new ArrayList<>());
        grid.addValueChangeListener(e -> counter.incrementAndGet());
        //when
        grid.editorCloseListener(new Param());
        //then
        assertEquals(1, counter.get());
    }

    @Test
    void whenContentIsReplacedThenValueChangeListenerIsCalled() {
        AtomicInteger counter = new AtomicInteger();
        GridParam grid = new GridParam(new ArrayList<>());
        grid.addValueChangeListener(e -> counter.incrementAndGet());
        //when
        grid.modifyItems(List.of(new Param()));
        //then
        assertEquals(1, counter.get());
    }

    @Test
    void whenGridHasNoFocusThenDoNotPasteToClipboard() {
        GridParam grid = new GridParam(new ArrayList<>());
        assertFalse(grid.pasteFromClipBoard(false));
    }

    @Test
    void whenGridHasFocusThenPasteToClipboard() {
        mockSession();
        GridParam grid = new GridParam(new ArrayList<>());
        grid.getElement().setAttribute("focused", "true");
        assertTrue(grid.pasteFromClipBoard(false));
    }

    @Test
    void whenGridHasNoFocusButIsForcedThenPasteToClipboard() {
        mockSession();
        GridParam grid = new GridParam(new ArrayList<>());
        assertTrue(grid.pasteFromClipBoard(true));
    }


    @Test
    void whenGridHasNoFocusThenDoNotCopyFromClipboard() {
        GridParam grid = new GridParam(new ArrayList<>());
        assertFalse(grid.copySelectedToClipBoard(false));
    }

    @Test
    void whenGridHasFocusThenCopyFromClipboard() {
        UI.setCurrent(mock(UI.class));
        GridParam grid = new GridParam(new ArrayList<>());
        grid.getElement().setAttribute("focused", "true");
        assertTrue(grid.copySelectedToClipBoard(false));
    }

    @Test
    void whenGridHasNoFocusButIsForcedThenCopyFromClipboard() {
        UI.setCurrent(mock(UI.class));
        GridParam grid = new GridParam(new ArrayList<>());
        assertTrue(grid.copySelectedToClipBoard(true));
    }


    @Test
    void whenGridHasNoFocusThenDoNotDeleteSelectedItems() {
        GridParam grid = new GridParam(new ArrayList<>());
        assertFalse(grid.deleteSelectedItems(false));
    }

    @Test
    void whenGridHasFocusThenDeleteSelectedItems() {
        GridParam grid = new GridParam(new ArrayList<>());
        grid.getElement().setAttribute("focused", "true");
        assertTrue(grid.deleteSelectedItems(false));
    }

    @Test
    void whenGridHasNoFocusButIsForcedThenDeleteSelectedItems() {
        GridParam grid = new GridParam(new ArrayList<>());
        assertTrue(grid.deleteSelectedItems(true));
    }


    private void mockSession() {
        VaadinSession.setCurrent(session);
        UserSession userSession = mock(UserSession.class);
        when(session.getAttribute(UserSession.class)).thenReturn(userSession);
        UISystem uiSystem = mock(UISystem.class);
        when(userSession.getUISystem()).thenReturn(uiSystem);
        ClipboardManager clipboard = mock(ClipboardManager.class);
        when(clipboard.readClipboard()).thenReturn(mock(CompletableFuture.class));
        when(uiSystem.getClipboardManager()).thenReturn(clipboard);

    }


}
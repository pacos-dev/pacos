package org.pacos.core.system.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.Serializable;

import static org.mockito.Mockito.*;

public class CouplerJSTest {

    private Page mockPage;
    private UI mockUI;

    @BeforeEach
    public void setup() {
        mockUI = mock(UI.class);
        mockPage = mock(Page.class);
        when(mockUI.getPage()).thenReturn(mockPage);
    }

    @Test
    void whenInitializeScriptsThenExecuteJsForAllScripts() {
        try (MockedStatic<UI> mockedUI = Mockito.mockStatic(UI.class)) {
            //given
            mockedUI.when(UI::getCurrent).thenReturn(mockUI);
            //when
            PacosJS.initializeScripts();

            // then
            InOrder inOrder = inOrder(mockPage);

            inOrder.verify(mockPage).executeJs("window.dockDraggable()");
            inOrder.verify(mockPage).executeJs(eq("window.systemClock($0)"),any(Serializable.class));
        }
    }

    @Test
    void whenPasteToClipboardThenExecuteJsForClipboard() {
        // given
        String clipboardValue = "Test Clipboard Value";
        UI.setCurrent(mockUI);
        // when
        PacosJS.pasteToClipboard(clipboardValue);

        // then
        verify(mockPage, times(1)).executeJs(
                contains("navigator.clipboard.writeText($0)"),
                eq(clipboardValue)
        );
    }
}
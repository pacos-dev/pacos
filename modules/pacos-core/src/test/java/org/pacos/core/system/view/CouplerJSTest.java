package org.pacos.core.system.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
            verify(mockPage, times(1)).executeJs(contains("document.addEventListener('mousemove'"));
            verify(mockPage, times(1)).executeJs("window.dockDraggable()");
            verify(mockPage, times(1)).executeJs(eq("window.systemClock()"), anyString());
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
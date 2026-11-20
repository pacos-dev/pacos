package org.pacos.base.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.dom.Element;

class TextHighlighterTest {

    @Test
    void whenHighlightCalledThenExecuteBrowserHighlighter() {
        Component target = mock(Component.class);
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        Element element = mock(Element.class);
        when(target.getUI()).thenReturn(Optional.of(ui));
        when(target.getElement()).thenReturn(element);
        when(ui.getPage()).thenReturn(page);

        TextHighlighter.highlight(target, "search", 2);

        verify(page).executeJs("window.pacos.textHighlighter.highlight($0, $1, $2)", element, "search", 2);
    }

    @Test
    void whenHighlightCalledWithBlankQueryThenClearBrowserHighlighter() {
        Component target = mock(Component.class);
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        Element element = mock(Element.class);
        when(target.getUI()).thenReturn(Optional.of(ui));
        when(target.getElement()).thenReturn(element);
        when(ui.getPage()).thenReturn(page);

        TextHighlighter.highlight(target, "  ");

        verify(page).executeJs("window.pacos.textHighlighter.clear($0)", element);
    }

    @Test
    void whenClearCalledThenClearBrowserHighlighter() {
        Component target = mock(Component.class);
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        Element element = mock(Element.class);
        when(target.getUI()).thenReturn(Optional.of(ui));
        when(target.getElement()).thenReturn(element);
        when(ui.getPage()).thenReturn(page);

        TextHighlighter.clear(target);

        verify(page).executeJs("window.pacos.textHighlighter.clear($0)", element);
    }

    @Test
    void whenHighlightCalledForDetachedComponentThenWaitForAttachment() {
        Component target = mock(Component.class);
        when(target.getUI()).thenReturn(Optional.empty());

        TextHighlighter.highlight(target, "search");

        verify(target).addAttachListener(org.mockito.ArgumentMatchers.any());
    }
}

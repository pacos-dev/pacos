package org.pacos.base.utils.component;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
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
    void whenDetachedComponentIsAttachedThenApplyPendingHighlight() {
        Component target = mock(Component.class);
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        Element element = mock(Element.class);
        AtomicReference<ComponentEventListener<AttachEvent>> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(0));
            return null;
        }).when(target).addAttachListener(org.mockito.ArgumentMatchers.any());
        when(target.getUI()).thenReturn(Optional.empty());
        when(target.getElement()).thenReturn(element);
        when(ui.getPage()).thenReturn(page);

        TextHighlighter.highlight(target, "search", 1);

        AttachEvent event = mock(AttachEvent.class);
        when(event.getSource()).thenReturn(target);
        when(event.getUI()).thenReturn(ui);
        listener.get().onComponentEvent(event);

        verify(page).executeJs("window.pacos.textHighlighter.highlight($0, $1, $2)", element, "search", 1);
    }

    @Test
    void whenComponentIsDetachedThenClearBrowserHighlighter() {
        Component target = mock(Component.class);
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        Element element = mock(Element.class);
        AtomicReference<ComponentEventListener<DetachEvent>> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(0));
            return null;
        }).when(target).addDetachListener(org.mockito.ArgumentMatchers.any());
        when(target.getUI()).thenReturn(Optional.empty());
        when(target.getElement()).thenReturn(element);
        when(ui.getPage()).thenReturn(page);

        TextHighlighter.highlight(target, "search");

        DetachEvent event = mock(DetachEvent.class);
        when(event.getSource()).thenReturn(target);
        when(event.getUI()).thenReturn(ui);
        listener.get().onComponentEvent(event);

        verify(page).executeJs("window.pacos.textHighlighter.clear($0)", element);
    }
}
package org.pacos.core.component.installer.view.steps.helper;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class BoxContent extends Div {
    public BoxContent() {
        setClassName("content");
    }

    public Span addTextLine(String text) {
        Span textLine = new Span(text);
        textLine.addClassName("text-line");
        add(textLine);
        return textLine;
    }
}

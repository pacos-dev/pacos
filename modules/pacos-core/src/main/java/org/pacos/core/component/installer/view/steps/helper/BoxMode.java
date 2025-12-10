package org.pacos.core.component.installer.view.steps.helper;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

public class BoxMode extends Div {
    public BoxMode() {
        setClassName("mode");
    }

    public void setImage(String image) {
        add(new Image(image, ""));
    }

    public void addListItem(String value) {
        Span span = new Span(value);
        span.setClassName("mode-value");
        add(span);
    }

    public void addTitle(String text) {
        Span span = new Span(text);
        span.setClassName("mode-title");
        add(span);
    }
}

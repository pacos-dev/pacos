package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;

public class SpanUtils extends com.vaadin.flow.component.html.Span {


    public SpanUtils(String text) {
        super(text);
    }

    public SpanUtils() {
    }

    public static SpanUtils ofClass(String className) {
        SpanUtils div = new SpanUtils();
        div.setClassName(className);
        return div;
    }

    public SpanUtils title() {
        this.addClassName("title");
        return this;
    }

    public SpanUtils withComponents(Component... components) {
        this.add(components);
        return this;
    }

    public SpanUtils withText(String text) {
        this.setText(text);
        return this;
    }

    public SpanUtils withStyle(String prop, String val) {
        getStyle().set(prop, val);
        return this;
    }

    public SpanUtils withClassName(String className) {
        this.addClassName(className);
        return this;
    }

    public SpanUtils paddingTop(int padding) {
        getStyle().set("padding-top", padding + "px");
        return this;
    }

    public SpanUtils withWidthFull() {
        super.setWidthFull();
        return this;
    }
}

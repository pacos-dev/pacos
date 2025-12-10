package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;

public class DivUtils extends com.vaadin.flow.component.html.Div {

    public static DivUtils ofClass(String... className) {
        DivUtils div = new DivUtils();
        for (String s : className) {
            div.addClassName(s);
        }
        return div;
    }

    public DivUtils withId(String id) {
        setId(id);
        return this;
    }

    public DivUtils withStyle(String prop, String val) {
        getStyle().set(prop, val);
        return this;
    }

    public DivUtils withText(String text) {
        setText(text);
        return this;
    }

    public DivUtils withComponents(Component... components) {
        this.add(components);
        return this;
    }

    public DivUtils withMarginAuto() {
        this.getStyle().set("margin", "auto");
        return this;
    }

    public DivUtils withClassName(String className) {
        this.addClassName(className);
        return this;
    }

    public DivUtils withWidth(String width) {
        setWidth(width);
        return this;
    }

    public DivUtils withHeight(String height) {
        setHeight(height);
        return this;
    }
}

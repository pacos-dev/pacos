package org.pacos.base.utils.component;

import com.vaadin.flow.component.html.Span;

public class LabelUtils extends Span {

    public LabelUtils(String text) {
        super(text);
    }

    public LabelUtils withStyle(String property, String value) {
        this.getStyle().set(property, value);
        return this;
    }

    public static Span tetiary(String value) {
        Span label = new Span(value);
        label.setClassName("tetiary");
        return label;
    }

    public static LabelUtils noSelectable(String value) {
        LabelUtils label = new LabelUtils(value);
        label.setClassName("no-select");
        return label;
    }

    public LabelUtils withClassName(String className){
        addClassName(className);
        return this;
    }
}

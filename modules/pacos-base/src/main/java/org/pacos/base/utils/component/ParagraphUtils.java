package org.pacos.base.utils.component;

public class ParagraphUtils extends com.vaadin.flow.component.html.Paragraph {

    public ParagraphUtils(String text) {
        super(text);
    }

    public ParagraphUtils withStyle(String prop, String val) {
        getStyle().set(prop, val);
        return this;
    }

    public ParagraphUtils withMarginAuto() {
        this.getStyle().set("margin", "auto");
        return this;
    }
}

package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;

/**
 * Formats how the list is displayed
 * NAME  VALUE
 * LONG NAME  ANOTHER VALUE
 * OTHER  TEST
 */
public class ListContent extends DivUtils {

    public ListContent() {
        withClassName("row");
        setWidthFull();
    }

    public ListContent(String className) {
        withClassName(className);
    }

    public DivUtils addRow(String title, String value, boolean bold) {
        SpanUtils valueSpan = new SpanUtils().withText(value)
                .withStyle("width", "45%")
                .withStyle("display", "inline-block");
        if (bold) {
            valueSpan.withStyle("font-weight", "600");
        }
        DivUtils content = new DivUtils()
                .withComponents(new SpanUtils()
                                .withStyle("text-align", "right")
                                .withStyle("padding-right", "10px")
                                .withStyle("width", "45%")
                                .withStyle("display", "inline-block")
                                .withText(title),
                        valueSpan);
        withComponents(content);
        return content;
    }

    public DivUtils addRow(String title, Component component) {
        DivUtils content = new DivUtils()
                .withComponents(SpanUtils.ofClass("title").withText(title),
                        component);
        withComponents(content);
        return content;
    }
}

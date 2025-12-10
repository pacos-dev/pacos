package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;

public class TabUtils extends Tab {

    private final Span labelComponent = new Span();

    public TabUtils(String label, Component... components) {
        this.labelComponent.setText(label);
        add(labelComponent);
        add(components);
    }


    public TabUtils setLabelText(String label) {
        this.labelComponent.setText(label);
        return this;
    }
}

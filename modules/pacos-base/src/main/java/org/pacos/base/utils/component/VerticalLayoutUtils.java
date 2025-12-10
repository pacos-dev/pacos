package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VerticalLayoutUtils extends VerticalLayout {

    public VerticalLayoutUtils(Component... children) {
        super(children);
    }

    public static VerticalLayoutUtils defaults(Component... components) {
        VerticalLayoutUtils layout = new VerticalLayoutUtils(components);
        configure(layout);
        return layout;
    }

    public VerticalLayoutUtils margin() {
        this.setMargin(true);
        return this;
    }

    public static VerticalLayout configure(VerticalLayout layout) {
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        return layout;
    }

    public VerticalLayout padding() {
        this.setPadding(true);
        return this;
    }
}

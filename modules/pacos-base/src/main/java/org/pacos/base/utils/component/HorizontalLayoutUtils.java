package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class HorizontalLayoutUtils extends HorizontalLayout {

    public HorizontalLayoutUtils() {
        configure(this);
    }

    public static HorizontalLayoutUtils defaults(Component... components) {
        HorizontalLayoutUtils layout = new HorizontalLayoutUtils();
        layout.add(components);
        configure(layout);
        return layout;
    }

    public static HorizontalLayout configure(HorizontalLayout layout) {
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        return layout;
    }

    public HorizontalLayoutUtils withHeight(float height, Unit unit) {
        setHeight(height, unit);
        return this;
    }
}

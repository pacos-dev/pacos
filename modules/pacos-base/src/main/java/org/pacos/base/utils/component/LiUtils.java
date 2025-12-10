package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.ListItem;

public class LiUtils extends ListItem {

    public LiUtils withComponents(Component... components) {
        super.add(components);
        return this;
    }
}

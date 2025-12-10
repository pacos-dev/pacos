package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.TabSheet;

public class TabSheetUtils extends TabSheet {

    public TabSheetUtils() {
        setSizeFull();
    }

    public TabSheetUtils withTab(String label, Component content) {
        TabUtils tab = new TabUtils(label);
        add(tab, content);
        return this;
    }

}

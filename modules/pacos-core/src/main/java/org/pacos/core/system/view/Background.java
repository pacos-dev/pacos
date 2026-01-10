package org.pacos.core.system.view;

import com.vaadin.flow.component.HasComponents;

public class Background{

    private Background() {
    }

    static void configure(HasComponents component, String location) {
        component.getElement().getStyle().set("background-image", "url(" + location + ")");
        component.getElement().getClassList().add("background");

    }
}

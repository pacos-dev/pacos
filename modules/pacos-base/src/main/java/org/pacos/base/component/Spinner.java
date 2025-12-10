package org.pacos.base.component;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;

/**
 * Displays a circle that spins endlessly
 */
@Tag("my-spinner")
public class Spinner extends HtmlComponent {

    public Spinner center(){
        this.getStyle().set("margin","auto");
        return this;
    }

    public Spinner appLoader() {
        setWidth("40px");
        setHeight("40px");
        getStyle().set("margin-left", "30px");
        getStyle().set("margin-bottom", "10px");
        getStyle().set("border", "4px solid var(--lumo-primary-text-color)");
        getStyle().set("border-bottom-color", "transparent");
        return this;
    }
}

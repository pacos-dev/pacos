package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

@SuppressWarnings("squid:S110")
public class InfoBox extends SpanUtils {

    public InfoBox(String info) {
        withClassName("alert")
                .withClassName("info")
                .withComponents(VaadinIcon.INFO_CIRCLE_O.create(),
                        new Text(info));
    }

    public InfoBox(Component... components) {
        withClassName("alert")
                .withClassName("info")
                .withComponents(VaadinIcon.INFO_CIRCLE_O.create(),
                        new Span(components));
    }
}

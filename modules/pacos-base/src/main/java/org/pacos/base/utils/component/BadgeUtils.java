package org.pacos.base.utils.component;

import com.vaadin.flow.component.html.Span;

public class BadgeUtils extends Span {

    private BadgeUtils() {
    }

    public static BadgeUtils create() {
        BadgeUtils badge = new BadgeUtils();
        badge.getElement().getThemeList().add("badge");
        badge.getStyle().set("margin-inline-start", "var(--lumo-space-xs)");
        badge.getStyle().set("background-color", "rgb(23, 136, 171)");
        badge.getStyle().set("font-size", "11px");
        badge.getStyle().set("color", "white");
        badge.addClassNames("s-badge");
        return badge;
    }

    public BadgeUtils withText(String text) {
        this.setText(text);
        return this;
    }
}

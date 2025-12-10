package org.pacos.common.view.menu;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;
import org.pacos.base.component.Color;
import org.pacos.base.utils.component.SpanUtils;

public class MenuItemBuilder {

    private static final String SCC_CLASS = "menuitem";
    private Color color;
    private IconFactory icon;
    private String label;
    private String shortcut;

    public MenuItemBuilder withColor(Color color) {
        this.color = color;
        return this;
    }

    public MenuItemBuilder withIconFactory(IconFactory iconFactory) {
        this.icon = iconFactory;
        return this;
    }

    public MenuItemBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    public MenuItemBuilder withShortcut(String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    public Span build() {
        Span item = new Span();
        item.addClassName(SCC_CLASS);

        if (icon != null) {
            Icon iconBase = icon.create();
            if (color != null) {
                iconBase.setColor(color.getColor());
            }
            item.add(iconBase);
        }
        if (label != null) {
            Span text = new Span(label);
            item.add(text);
            if (color != null) {
                text.getStyle().set("text-shadow", "0px 0px 2px " + color.getColorAlpha(0.3));
            }
        }
        if (shortcut != null) {
            item.add(SpanUtils.ofClass("short_cut").withText("[" + shortcut + "]"));
        }
        return item;
    }
}

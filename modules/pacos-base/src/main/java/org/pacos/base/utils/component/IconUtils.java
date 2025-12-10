package org.pacos.base.utils.component;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.base.component.Color;


public class IconUtils extends Icon {

    public static Icon colorIcon(IconFactory icon, Color color) {
        final Icon ic = icon.create();
        ic.getElement().setAttribute(color.name(), color.getColor());
        return ic;
    }

    public static Icon colorIcon(IconFactory icon, Color color, String tooltip) {
        final Icon ic = colorIcon(icon, color);
        ic.setTooltipText(tooltip);
        return ic;
    }

    public static Icon tetiaryIcon(VaadinIcon icon) {
        final Icon ic = new Icon(icon);
        ic.setColor("var(--lumo-tertiary-text-color)");
        return ic;
    }
}

package org.pacos.common.view.menu;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import org.pacos.base.component.Color;
import org.pacos.base.utils.component.IconUtils;

/**
 * Stores access to Vaadin MenuItem based on MenuElement configuration.
 * Allows to quickly change visibility of the items
 */
public class ModuleMenuBar extends MenuBar {

    private final Map<MenuElement, MenuItem> menuItems = new HashMap<>();

    public ModuleMenuBar() {
        this.addClassName("menu-block");
        this.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        this.addThemeVariants(MenuBarVariant.LUMO_SMALL);
    }

    protected void modifyEnabled(boolean enabled, MenuElement... elements) {
        for (MenuElement el : elements) {
            final MenuItem menuItem = menuItems.get(el);
            menuItem.setEnabled(enabled);
        }
    }

    protected void addMenuItem(MenuElement element, MenuItem item) {
        menuItems.put(element, item);
    }

    protected static Icon colorIcon(IconFactory icon, Color color) {
        return IconUtils.colorIcon(icon, color);
    }

    protected MenuItem addMenuItem(MenuElement element, Component component,
                                   ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        MenuItem addedItem = addItem(component, element.getDisplayName(), clickListener);
        menuItems.put(element, addedItem);
        return addedItem;
    }

    protected MenuItem getMenuItem(MenuElement menuElement) {
        return menuItems.get(menuElement);
    }

    protected void addSpace() {
        Div div = new Div();
        final MenuItem menuItem = addItem(div);
        menuItem.getElement().getStyle().set("width", "50px");
        menuItem.setEnabled(false);
    }

    protected void addDivider() {
        Div div = new Div();
        final MenuItem menuItem = addItem(div);
        menuItem.getElement().setProperty("part", "divider");
        menuItem.setEnabled(false);
    }
}

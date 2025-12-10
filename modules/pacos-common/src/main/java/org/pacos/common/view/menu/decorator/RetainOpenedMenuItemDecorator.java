package org.pacos.common.view.menu.decorator;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;

/**
 * Do not close the submenu when user move mouse outside the submenu window
 */
public final class RetainOpenedMenuItemDecorator {
    public static final String CHECKED_ATTR = "menu-item-checked";

    private RetainOpenedMenuItemDecorator() {
    }

    public static void keepOpenOnClick(MenuItem menuItem) {
        menuItem.getElement().addEventListener("click",
                e -> onClickEvent(menuItem)).addEventData("event.preventDefault()");
    }

    public static void keepOpenOnClick(SubMenu subMenu) {
        subMenu.getItems().forEach(RetainOpenedMenuItemDecorator::keepOpenOnClick);
    }

    static void onClickEvent(MenuItem menuItem) {
        if (!menuItem.isCheckable()) {
            return;
        }
        // needed because UI change is also prevented
        if (menuItem.isChecked()) {
            menuItem.getElement().executeJs("this.setAttribute('" + CHECKED_ATTR + "', '')");
        } else {
            menuItem.getElement().executeJs("this.removeAttribute('" + CHECKED_ATTR + "')");
        }
    }
}

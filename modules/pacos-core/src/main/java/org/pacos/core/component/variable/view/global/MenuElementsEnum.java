package org.pacos.core.component.variable.view.global;

import org.pacos.common.view.menu.MenuElement;

public enum MenuElementsEnum implements MenuElement {

    ADD("Add new variable"),
    REMOVE("Remove selected variable");

    private final String displayName;

    MenuElementsEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}

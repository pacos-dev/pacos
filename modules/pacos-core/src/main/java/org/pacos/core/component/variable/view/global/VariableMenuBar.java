package org.pacos.core.component.variable.view.global;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.base.component.Color;
import org.pacos.base.session.UserSession;
import org.pacos.common.view.menu.ModuleMenuBar;
import org.pacos.core.component.variable.VariablePermissions;
import org.pacos.core.component.variable.event.global.AddNewGlobalVariableEvent;
import org.pacos.core.component.variable.event.global.RemoveGlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

public class VariableMenuBar extends ModuleMenuBar {

    public VariableMenuBar(GlobalVariableSystem system) {
        super();

        MenuItem addItem = addMenuItem(MenuElementsEnum.ADD,
                colorIcon(VaadinIcon.PLUS, Color.GREEN),
                e -> AddNewGlobalVariableEvent.fireEvent(system));

        MenuItem removeItem = addMenuItem(MenuElementsEnum.REMOVE, colorIcon(VaadinIcon.TRASH, Color.RED),
                e -> RemoveGlobalVariableEvent.fireEvent(system));

        if (!UserSession.getCurrent().hasPermission(VariablePermissions.EDIT_GLOBAL_VARIABLE)) {
            removeItem.setEnabled(false);
            addItem.setEnabled(false);
        }
    }

}

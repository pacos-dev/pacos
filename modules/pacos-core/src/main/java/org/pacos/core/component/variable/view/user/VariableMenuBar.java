package org.pacos.core.component.variable.view.user;

import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.common.view.menu.ModuleMenuBar;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public class VariableMenuBar extends ModuleMenuBar {

    public VariableMenuBar(UserVariableSystem system) {
        super();

        addMenuItem(() -> "Copy selected variables to clipboard [ctrl + c]",
                VaadinIcon.COPY_O.create(),
                e -> system.notify(UserVariableEvent.COPY_SHORTCUT_EVENT));

        addMenuItem(() -> "Paste variables from clipboard [ctrl + v]",
                VaadinIcon.PASTE.create(),
                e -> system.notify(UserVariableEvent.PASTE_SHORTCUT_EVENT));
    }
}

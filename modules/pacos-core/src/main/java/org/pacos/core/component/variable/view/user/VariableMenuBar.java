package org.pacos.core.component.variable.view.user;

import org.pacos.base.window.shortcut.ShortcutPlatformResolver;
import org.pacos.common.view.menu.ModuleMenuBar;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

import com.vaadin.flow.component.icon.VaadinIcon;

public class VariableMenuBar extends ModuleMenuBar {

    public VariableMenuBar(UserVariableSystem system) {
        super();

        String modifier = ShortcutPlatformResolver.getModifierLabel();
        addMenuItem(() -> "Copy selected variables to clipboard [" + modifier + " + C]",
                VaadinIcon.COPY.create(),
                e -> system.notify(UserVariableEvent.COPY_SHORTCUT_EVENT));

        addMenuItem(() -> "Paste variables from clipboard [" + modifier + " + V]",
                VaadinIcon.PASTE.create(),
                e -> system.notify(UserVariableEvent.PASTE_SHORTCUT_EVENT));
    }
}

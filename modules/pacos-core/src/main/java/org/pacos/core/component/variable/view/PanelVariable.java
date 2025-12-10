package org.pacos.core.component.variable.view;

import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.pacos.base.utils.component.SplitterUtils;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.config.VariableConfig;
import org.pacos.core.component.variable.view.user.CollectionMenuBar;
import org.pacos.core.component.variable.view.user.UserVariableCollectionGrid;
import org.pacos.core.component.variable.view.user.UserVariableTabSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PanelVariable extends DesktopWindow {

    private final transient UserVariableSystem system;

    @Autowired
    public PanelVariable(VariableConfig moduleConfig,
                         UserVariableCollectionProxy userVariableCollectionProxy,
                         UserVariableProxy userVariableProxy) {
        super(moduleConfig);

        this.system = new UserVariableSystem(this, uiSystem, userVariableCollectionProxy, userVariableProxy);

        add(new SplitterUtils(
                VerticalLayoutUtils.defaults(new CollectionMenuBar(system), new UserVariableCollectionGrid(system)),
                new UserVariableTabSheet(system),
                30).orientation(SplitLayout.Orientation.HORIZONTAL));

        super.registerShortcut(ShortcutType.SAVE, e -> system.notify(UserVariableEvent.SAVE_SHORTCUT_EVENT));
        super.registerShortcut(ShortcutType.DELETE.getShortcut(), e -> system.notify(UserVariableEvent.DELETE_SHORTCUT_EVENT)).setBrowserDefaultAllowed(true);
        super.registerShortcut(ShortcutType.COPY.getShortcut(), e -> system.notify(UserVariableEvent.COPY_SHORTCUT_EVENT)).setBrowserDefaultAllowed(true);
        super.registerShortcut(ShortcutType.PASTE.getShortcut(), e -> system.notify(UserVariableEvent.PASTE_SHORTCUT_EVENT)).setBrowserDefaultAllowed(true);
    }

    @Override
    public void close() {
        super.close();
        system.notify(UserVariableEvent.CLOSE_ALL_TABS);
    }
}

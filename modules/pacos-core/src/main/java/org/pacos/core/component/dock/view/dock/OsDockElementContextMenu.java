package org.pacos.core.component.dock.view.dock;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import org.pacos.base.component.Theme;
import org.pacos.base.utils.component.CheckboxUtils;
import org.pacos.base.window.config.WindowConfig;

/**
 * Context menu for dock element opened after right click of the mouse
 */
public class OsDockElementContextMenu extends ContextMenu {

    private final OsDockElement osElement;
    private final MenuItem menuClose;

    OsDockElementContextMenu(OsDockElement osDockElement, WindowConfig config) {
        this.osElement = osDockElement;

        addItem("Open", e -> osDockElement.openOrHideWindowOnUserClickEvent());

        if (config.isApplication() && osDockElement.getModuleConfig().isApplication()) {
                addItem(new CheckboxUtils("Store activator",
                        osElement.isStored(),
                        e -> osDockElement.storeActivator(e.getValue())).withStyle("color", "white")
                        .withStyle("margin-left", "-23px"));
            }

        menuClose = addItem("Close", e -> osDockElement.close());
        setTarget(osDockElement);
        getElement().getThemeList().add(Theme.DOCK.getName());
        addOpenedChangeListener(e -> menuClose.setVisible(osElement.isActive()));
    }

    public static void configureFor(OsDockElement osDockElement, WindowConfig config) {
        new OsDockElementContextMenu(osDockElement, config);
    }


}

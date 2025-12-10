package org.pacos.core.component.variable.view.user;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import org.pacos.common.view.menu.MenuItemBuilder;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.event.user.CloneCollectionEvent;
import org.pacos.core.component.variable.event.user.RemoveCollectionEvent;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public class UserCollectionContextMenu {

    private final UserVariableSystem system;
    private final UserVariableCollectionGrid grid;

    public UserCollectionContextMenu(UserVariableCollectionGrid grid, UserVariableSystem system) {
        this.system = system;
        this.grid = grid;
        configureFor(grid.addContextMenu());

    }

    private void configureFor(GridContextMenu<UserVariableCollectionDTO> menu) {
        menu.addGridContextMenuOpenedListener(e -> {
            if (e.getItem().isEmpty()) {
                menu.close();
                grid.asSingleSelect().setValue(null);
            } else {
                grid.asSingleSelect().setValue(e.getItem().get());
            }
        });
        menu.addItem("Open", event -> {
            event.getItem().ifPresent(e ->
                    system.notify(UserVariableEvent.OPEN_COLLECTION_VARIABLE_TAB, e));
            menu.close();
        });
        menu.addItem("Clone", event -> {
            event.getItem().ifPresent(e -> CloneCollectionEvent.fireEvent(system));
            menu.close();
        });
        menu.addItem("Edit name", event -> {
            if (event.getItem().isPresent() && !event.getItem().get().isGlobal()) {
                event.getItem().ifPresent(grid::editItem);
            }
            menu.close();
        });
        menu.add(new Hr());
        menu.addItem(new MenuItemBuilder()
                .withLabel("Remove").withShortcut("del").build(), event -> {
            event.getItem().ifPresent(e -> RemoveCollectionEvent.fireEvent(system));
            menu.close();
        });
    }
}

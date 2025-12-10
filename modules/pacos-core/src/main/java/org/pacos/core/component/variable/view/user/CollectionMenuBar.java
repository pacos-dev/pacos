package org.pacos.core.component.variable.view.user;

import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.base.component.Color;
import org.pacos.common.view.menu.ModuleMenuBar;
import org.pacos.core.component.variable.event.user.AddNewCollectionEvent;
import org.pacos.core.component.variable.event.user.CloneCollectionEvent;
import org.pacos.core.component.variable.event.user.RemoveCollectionEvent;
import org.pacos.core.component.variable.event.user.ShowVariableHelpWindowEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public class CollectionMenuBar extends ModuleMenuBar {

    public CollectionMenuBar(UserVariableSystem system) {
        super();

        addMenuItem(() -> "Add new collection",
                colorIcon(VaadinIcon.PLUS, Color.GREEN),
                e -> AddNewCollectionEvent.fireEvent(system));

        addMenuItem(() -> "Clone collection",
                colorIcon(VaadinIcon.COPY_O, Color.BLACK_LITGHT),
                e -> CloneCollectionEvent.fireEvent(system));


        addMenuItem(() -> "Remove collection [del]",
                colorIcon(VaadinIcon.TRASH, Color.RED),
                e -> RemoveCollectionEvent.fireEvent(system));
        addMenuItem(() -> "About variables",
                colorIcon(VaadinIcon.QUESTION_CIRCLE_O, Color.YELLOW),
                e -> ShowVariableHelpWindowEvent.fireEvent(system));
    }
}

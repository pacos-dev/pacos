package org.pacos.core.component.variable.view.plugin;

import com.vaadin.flow.component.select.Select;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;

public class CollectionSelect extends Select<UserVariableCollectionDTO> {
    public CollectionSelect() {
        setEmptySelectionAllowed(true);
        setItemLabelGenerator(e -> {
            if (e == null) {
                return "None";
            } else {
                return e.getDisplayName();
            }
        });
        setWidthFull();
    }
}

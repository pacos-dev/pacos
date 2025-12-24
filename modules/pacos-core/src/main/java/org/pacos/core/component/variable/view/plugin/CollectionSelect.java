package org.pacos.core.component.variable.view.plugin;

import com.vaadin.flow.component.select.Select;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;

import java.util.List;

public class CollectionSelect extends Select<UserVariableCollectionDTO> {

    public CollectionSelect(List<UserVariableCollectionDTO> collectionDTOList) {
        setItems(collectionDTOList);
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

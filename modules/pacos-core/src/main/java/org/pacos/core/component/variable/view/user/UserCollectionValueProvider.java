package org.pacos.core.component.variable.view.user;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.function.ValueProvider;
import org.pacos.base.utils.component.SpanUtils;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;

public class UserCollectionValueProvider implements ValueProvider<UserVariableCollectionDTO, Component> {

    @Override
    public Component apply(UserVariableCollectionDTO collectionDTO) {
        if (collectionDTO.isGlobal()) {
            return new SpanUtils("Global").withStyle("font-weight", "bold");
        }

        return new Text(collectionDTO.getName());
    }
}

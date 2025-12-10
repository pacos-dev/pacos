package org.pacos.core.component.variable.dto.mapper;

import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.vaadin.addons.variablefield.data.Variable;

public class UserVariableToVariableMapper {

    private UserVariableToVariableMapper() {
    }

    public static Variable map(UserVariableDTO v) {
        return new Variable(v.getId(), v.getName(), v.getInitialValue(), v.getCurrentValue(), null);
    }
}

package org.pacos.core.component.variable.dto.mapper;

import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.vaadin.addons.variablefield.data.Variable;

public class SystemVariableToVariableMapper {

    private SystemVariableToVariableMapper() {
    }

    public static Variable map(SystemVariableDTO v) {
        return new Variable(v.getId(), v.getName(), v.getDescription(), null, null);
    }

    public static Variable map(SystemVariableDTO v, String result) {
        return new Variable(v.getId(), v.getName(), result, null, null);
    }
}

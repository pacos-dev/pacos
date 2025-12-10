package org.pacos.core.component.variable.dto.mapper;

import org.pacos.core.component.variable.domain.SystemVariable;
import org.pacos.core.component.variable.dto.SystemVariableDTO;

public class SystemVariableMapper {

    private SystemVariableMapper() {
    }

    public static SystemVariableDTO map(SystemVariable variable) {
        SystemVariableDTO systemVariableDTO = new SystemVariableDTO();
        systemVariableDTO.setId(variable.getId());
        systemVariableDTO.setName(variable.getName());
        systemVariableDTO.setDescription(variable.getDescription());
        systemVariableDTO.setJs(variable.getJs());

        return systemVariableDTO;
    }
}

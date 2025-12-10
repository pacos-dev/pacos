package org.pacos.core.component.variable.dto.mapper;

import org.pacos.core.component.variable.domain.UserVariable;
import org.pacos.core.component.variable.dto.UserVariableDTO;

public class UserVariableMapper {

    private UserVariableMapper() {
    }

    public static UserVariable bindFromDTO(UserVariableDTO resource, UserVariable existing) {
        existing.setName(resource.getName());
        existing.setInitialValue(resource.getInitialValue());
        existing.setCurrentValue(resource.getCurrentValue());
        existing.setEnabled(resource.isEnabled());
        existing.setCollectionId(resource.getCollectionId());
        existing.setId(resource.getId());
        return existing;
    }

    public static UserVariable bindFromDTO(UserVariableDTO resource) {
        UserVariable variable = new UserVariable();
        return bindFromDTO(resource, variable);
    }

    public static UserVariableDTO map(UserVariable variable) {
        UserVariableDTO dto = new UserVariableDTO();
        dto.setId(variable.getId());
        dto.setName(variable.getName());
        dto.setEnabled(variable.isEnabled());
        dto.setInitialValue(variable.getInitialValue());
        dto.setCurrentValue(variable.getCurrentValue());
        dto.setCollectionId(variable.getCollectionId());
        return dto;
    }
}

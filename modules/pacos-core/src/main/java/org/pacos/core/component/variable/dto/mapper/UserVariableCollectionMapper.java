package org.pacos.core.component.variable.dto.mapper;

import org.pacos.core.component.variable.domain.UserVariableCollection;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;

public class UserVariableCollectionMapper {

    private UserVariableCollectionMapper() {
    }

    public static void bindFromDTO(UserVariableCollectionDTO resource, UserVariableCollection existing) {
        existing.setName(resource.getName());
        if (existing.getUserId() == null) {
            existing.setUserId(resource.getUserId());
        }
    }
    public static UserVariableCollectionDTO map(UserVariableCollection collection) {
        return UserVariableCollectionDTO.builder()
                .global(collection.isGlobal())
                .userId(collection.getUserId())
                .name(collection.getName())
                .id(collection.getId()).build();
    }
}

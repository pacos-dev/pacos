package org.pacos.core.component.variable.dto.mapper;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.domain.UserVariableCollection;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserVariableCollectionMapperTest {

    @Test
    void whenBindFromDTOThenExistingObjectIsUpdated() {
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder()
                .name("NewName")
                .userId(100)
                .build();

        UserVariableCollection existing = new UserVariableCollection();
        existing.setUserId(null);

        UserVariableCollectionMapper.bindFromDTO(dto, existing);

        assertEquals("NewName", existing.getName());
        assertEquals(100, existing.getUserId());
    }

    @Test
    void whenBindFromDTOWithExistingUserIdThenUserIdIsNotOverwritten() {
        UserVariableCollectionDTO dto = UserVariableCollectionDTO.builder()
                .name("NewName")
                .userId(200)
                .build();

        UserVariableCollection existing = new UserVariableCollection();
        existing.setUserId(100);

        UserVariableCollectionMapper.bindFromDTO(dto, existing);

        assertEquals("NewName", existing.getName());
        assertEquals(100, existing.getUserId());
    }

    @Test
    void whenMapCollectionThenDTOIsCreated() {
        UserVariableCollection collection = new UserVariableCollection();
        collection.setId(1);
        collection.setName("CollectionName");
        collection.setUserId(100);
        collection.setGlobal(true);

        UserVariableCollectionDTO dto = UserVariableCollectionMapper.map(collection);

        assertEquals(1, dto.getId());
        assertEquals("CollectionName", dto.getName());
        assertEquals(100, dto.getUserId());
        assertTrue(dto.isGlobal());
    }
}

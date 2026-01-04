package org.pacos.core.component.variable.service;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.domain.UserVariable;
import org.pacos.core.component.variable.domain.UserVariableCollection;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.repository.UserVariableCollectionRepository;
import org.pacos.core.component.variable.repository.UserVariableRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserVariableCollectionServiceTest extends IntegrationTestContext {

    @Autowired
    private UserVariableCollectionService userVariableCollectionService;
    @Autowired
    private UserVariableCollectionRepository collectionRepository;
    @Autowired
    private UserVariableRepository userVariableRepository;

    @Test
    void whenGetForUserThenInitializeDefaultCollections() {
        assertEquals(0, collectionRepository.count());
        //when
        List<UserVariableCollection> defaultCollections = userVariableCollectionService.getForUser(1);
        //then
        assertTrue(defaultCollections.stream().anyMatch(UserVariableCollection::isGlobal));
        assertTrue(defaultCollections.stream().anyMatch(c -> !c.isGlobal()));
    }

    @Test
    void whenGetForUserThenInitializeGlobalCollection() {
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().userId(1).name("test").build();
        //when
        userVariableCollectionService.save(collectionDTO);
        //and
        List<UserVariableCollection> collections = userVariableCollectionService.getForUser(1);
        //then
        assertTrue(collections.stream().anyMatch(UserVariableCollection::isGlobal));
        assertTrue(collections.stream().anyMatch(c -> c.getName().equals(collectionDTO.getName())));
    }


    @Test
    void whenSaveExistingCollectionThenUpdateFields() {
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().userId(1).name("test").build();
        collectionDTO = userVariableCollectionService.save(collectionDTO);
        //when
        collectionDTO.setName("test2");
        userVariableCollectionService.save(collectionDTO);
        //then
        UserVariableCollection collection = userVariableCollectionService.findById(collectionDTO.getId()).orElseThrow();
        assertEquals("test2", collection.getName());
    }

    @Test
    void whenDeleteExistingCollectionThenDeleteAlsoVariables() {
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().userId(1).name("test").build();
        collectionDTO = userVariableCollectionService.save(collectionDTO);
        UserVariable variable = new UserVariable();
        variable.setName("test");
        userVariableCollectionService.saveVariables(collectionDTO.getId(), List.of(variable));
        assertEquals(1, userVariableRepository.count());
        //when
        userVariableCollectionService.remove(collectionDTO);
        //then
        assertEquals(0, userVariableRepository.count());
        assertEquals(0, collectionRepository.count());
    }

    @Test
    void whenCloneThenDuplicateCollectionAndVariables() {
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().userId(1).name("test").build();
        collectionDTO = userVariableCollectionService.save(collectionDTO);
        UserVariable variable = new UserVariable();
        variable.setName("test");
        userVariableCollectionService.saveVariables(collectionDTO.getId(), List.of(variable));
        //when
        userVariableCollectionService.clone(collectionDTO);
        //then
        assertEquals(2, userVariableRepository.count());
        assertEquals(2, collectionRepository.count());
    }

}
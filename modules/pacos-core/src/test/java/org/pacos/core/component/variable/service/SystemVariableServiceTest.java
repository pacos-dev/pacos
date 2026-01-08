package org.pacos.core.component.variable.service;

import org.config.IntegrationTestContext;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.domain.SystemVariable;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.repository.SystemVariableRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemVariableServiceTest extends IntegrationTestContext {

    @Autowired
    private SystemVariableService systemVariableService;

    @Autowired
    private SystemVariableRepository systemVariableRepository;

    @Test
    void whenLoadAllVariablesThenReturnListOfSystemVariables() {
        SystemVariable entity = new SystemVariable();
        entity.setName("name");
        systemVariableRepository.save(entity);
        //when
        List<SystemVariable> variableList = systemVariableService.loadAllVariables();
        //then
        assertEquals(23, variableList.size());
    }

    @Test
    void whenInitNewVariableThenReturnNewSystemVariable() {
        //when
        SystemVariable systemVariable = systemVariableService.initNewVariable();
        //then
        assertEquals("$variable_22", systemVariable.getName());
    }

    @Test
    void whenSaveThenCreateNewVariable(){
        SystemVariableDTO dto = new SystemVariableDTO();
        dto.setName("name");
        dto.setDescription("description");
        dto.setJs("js script");
        //when
        systemVariableService.save(dto);
        //then
        SystemVariable entity = systemVariableRepository.findByName("name").orElseThrow();
        assertEquals("name", entity.getName());
        assertEquals("description", entity.getDescription());
        assertEquals("js script", entity.getJs());
    }

    @Test
    void whenSaveExistingVariableThenUpdateFields(){
        SystemVariable systemVariable = systemVariableService.initNewVariable();
        SystemVariableDTO dto = new SystemVariableDTO();
        dto.setId(systemVariable.getId());
        dto.setName("name");
        dto.setDescription("description");
        dto.setJs("js script");
        //when
        systemVariableService.save(dto);
        //then
        SystemVariable entity = systemVariableRepository.getReferenceById(systemVariable.getId());
        assertEquals("name", entity.getName());
        assertEquals("description", entity.getDescription());
        assertEquals("js script", entity.getJs());
    }

    @Test
    void whenIsUniqueThenReturnTrueIfNameNotExists(){
        SystemVariable systemVariable = systemVariableService.initNewVariable();
        //when
        assertTrue(systemVariableService.isUnique(systemVariable.getId(),"new name"));
    }

    @Test
    void whenIsUniqueThenReturnTrueIfCheckTheSameVariable(){
        SystemVariable systemVariable = systemVariableService.initNewVariable();
        //when
        assertTrue(systemVariableService.isUnique(systemVariable.getId(),systemVariable.getName()));
    }

    @Test
    void whenIsUniqueThenReturnFalseIfNameAlreadyExists(){
        SystemVariable systemVariable = systemVariableService.initNewVariable();
        SystemVariable systemVariable2 = systemVariableService.initNewVariable();
        //when
        assertFalse(systemVariableService.isUnique(systemVariable.getId(),systemVariable2.getName()));
    }

    @Test
    void whenRemoveByDtoThenRemoveFromSystemVariable(){
        SystemVariable systemVariable = systemVariableService.initNewVariable();
        SystemVariableDTO dto = new SystemVariableDTO();
        dto.setId(systemVariable.getId());
        //when
        systemVariableService.remove(dto);
        //then
        assertFalse(systemVariableRepository.existsById(dto.getId()));
    }

    @Test
    void whenFindByNameThenReturnSystemVariable(){
        //when
        SystemVariable variable = systemVariableService.initNewVariable();
        //then
        SystemVariable found = systemVariableService.findByName(variable.getName()).orElseThrow();
        assertEquals(variable.getName(), found.getName());
    }
}
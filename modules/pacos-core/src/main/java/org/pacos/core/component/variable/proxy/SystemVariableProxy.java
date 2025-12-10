package org.pacos.core.component.variable.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.pacos.core.component.variable.domain.SystemVariable;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.dto.mapper.SystemVariableMapper;
import org.pacos.core.component.variable.dto.mapper.SystemVariableToVariableMapper;
import org.pacos.core.component.variable.service.SystemVariableService;
import org.springframework.stereotype.Component;
import org.vaadin.addons.variablefield.data.Variable;

@Component
@AllArgsConstructor
public class SystemVariableProxy {

    private final SystemVariableService systemVariableService;

    public List<SystemVariableDTO> loadVariables() {
        List<SystemVariableDTO> mappedList = new ArrayList<>();
        for (SystemVariable systemVariable : systemVariableService.loadAllVariables()) {
            mappedList.add(SystemVariableMapper.map(systemVariable));
        }
        return mappedList;
    }

    public SystemVariableDTO initNewVariable() {
        return SystemVariableMapper.map(systemVariableService.initNewVariable());
    }

    public void save(SystemVariableDTO dto) {
        systemVariableService.save(dto);
    }

    public boolean isUnique(Integer id, String name) {
        return systemVariableService.isUnique(id, name);
    }

    public void removeVariable(SystemVariableDTO dto) {
        systemVariableService.remove(dto);
    }

    public List<Variable> loadSystemVariable() {
        return loadVariables().stream()
                .map(SystemVariableToVariableMapper::map)
                .toList();
    }

    public Optional<SystemVariableDTO> findVariable(String variableName) {
        Optional<SystemVariable> byName = systemVariableService.findByName(variableName);
        return byName.map(SystemVariableMapper::map);
    }
}

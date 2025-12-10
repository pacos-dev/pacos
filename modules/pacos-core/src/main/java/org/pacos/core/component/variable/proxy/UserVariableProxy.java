package org.pacos.core.component.variable.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.variable.domain.UserVariable;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.dto.mapper.UserVariableMapper;
import org.pacos.core.component.variable.service.UserVariableCollectionService;
import org.pacos.core.component.variable.service.UserVariableService;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserVariableProxy {

    private final UserVariableService userVariableService;
    private final UserVariableCollectionService collectionService;

    public List<UserVariableDTO> loadVariables(Integer variableCollectionId) {
        return new ArrayList<>(userVariableService.loadForCollection(variableCollectionId).stream().map(UserVariableMapper::map)
                .toList());
    }

    public List<UserVariableDTO> loadGlobalVariables(Integer userId) {
        return userVariableService.loadGlobalVariables(userId).stream().map(UserVariableMapper::map)
                .toList();
    }

    public Optional<UserVariableDTO> loadGlobalVariablesByName(Integer userId, String name) {
        return userVariableService.loadGlobalVariablesByName(userId, name)
                .flatMap(variable -> Optional.of(UserVariableMapper.map(variable)));
    }

    public UserVariableDTO saveVariable(UserVariableDTO userVariableDTO) {
        userVariableDTO.setId(userVariableService.save(userVariableDTO));
        return userVariableDTO;
    }


    public List<UserVariableDTO> saveForCollection(Integer collectionId, List<UserVariableDTO> variablesDTO) {
        try {
            List<UserVariable> variables = new ArrayList<>();
            for (UserVariableDTO variableDTO : variablesDTO) {
                variables.add(UserVariableMapper.bindFromDTO(variableDTO));
            }
            return new ArrayList<>(collectionService.saveVariables(collectionId, variables)
                    .stream().map(UserVariableMapper::map).toList());
        } catch (Exception e) {
            throw new PacosException("Error occurred during save collection", e);
        }
    }

    public Optional<UserVariableDTO> loadVariable(Integer collection, String variableName) {
        return userVariableService.findByCollectionIdAndName(collection, variableName)
                .flatMap(variable -> Optional.of(UserVariableMapper.map(variable)));
    }
}

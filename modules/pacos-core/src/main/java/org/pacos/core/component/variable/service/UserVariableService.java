package org.pacos.core.component.variable.service;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.pacos.core.component.variable.domain.UserVariable;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.dto.mapper.UserVariableMapper;
import org.pacos.core.component.variable.repository.UserVariableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserVariableService {

    private final UserVariableRepository userVariableRepository;


    @Transactional("coreTransactionManager")
    public int save(UserVariableDTO dto) {
        UserVariable existing;
        if (dto.getId() != null) {
            existing = userVariableRepository.findById(dto.getId()).orElse(new UserVariable());
        } else {
            existing = new UserVariable();
        }

        UserVariableMapper.bindFromDTO(dto, existing);
        return userVariableRepository.saveAndFlush(existing).getId();
    }

    public List<UserVariable> loadForCollection(Integer variableCollectionId) {
        return userVariableRepository.findAllByCollectionId(variableCollectionId);
    }

    public Optional<UserVariable> findByCollectionIdAndName(Integer collection, String variableName) {
        return userVariableRepository.findByCollectionIdAndName(collection, variableName);
    }

    public List<UserVariable> loadGlobalVariables(Integer userId) {
        return userVariableRepository.findGlobalForUser(userId);
    }

    public Optional<UserVariable> loadGlobalVariablesByName(Integer userId, String name) {
        return userVariableRepository.loadGlobalVariablesByName(userId, name);
    }

    public void deleteAllByCollectionId(Integer id) {
        userVariableRepository.deleteAllByCollectionId(id);
    }

    public List<UserVariable> cloneForCollection(Integer newCollectionId, UserVariableCollectionDTO dto) {
        List<UserVariable> variables = loadForCollection(dto.getId());
        List<UserVariable> copyVariables = loadForCollection(dto.getId());
        for (UserVariable existing : variables) {
            UserVariable v = new UserVariable();
            v.setCollectionId(newCollectionId);
            v.setEnabled(existing.isEnabled());
            v.setName(existing.getName());
            v.setCurrentValue(existing.getCurrentValue());
            v.setInitialValue(existing.getInitialValue());
            copyVariables.add(v);
        }
        return userVariableRepository.saveAll(copyVariables);
    }
}

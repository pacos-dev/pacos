package org.pacos.core.component.variable.service;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.pacos.core.component.variable.domain.SystemVariable;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.repository.SystemVariableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SystemVariableService {

    private final SystemVariableRepository systemVariableRepository;
    private static final String DEFAULT_CONTENT =
            """
                    //This is an example how to generates random string with defined length
                    //let length = 4;
                    //const characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                    // let result = "";
                    // for (let i = 0; i < length; i++) {
                    //   const randomIndex = Math.floor(Math.random() * characters.length);
                    //   result += characters.charAt(randomIndex);
                    // }
                    // return result;


                    //The return statement must be added at the end of each script
                    return 'My static value'""";

    public List<SystemVariable> loadAllVariables() {
        return systemVariableRepository.findAll();
    }

    public SystemVariable initNewVariable() {
        long count = systemVariableRepository.count();
        SystemVariable newVariable = new SystemVariable();
        newVariable.setName("$variable_" + count);
        newVariable.setJs(DEFAULT_CONTENT);
        return systemVariableRepository.save(newVariable);
    }

    @Transactional("coreTransactionManager")
    public void save(SystemVariableDTO dto) {
        SystemVariable existing;
        if (dto.getId() != null) {
            existing = systemVariableRepository.findById(dto.getId()).orElse(new SystemVariable());
        } else {
            existing = new SystemVariable();
        }

        bindFromDTO(dto, existing);
        systemVariableRepository.saveAndFlush(existing);
    }

    public boolean isUnique(Integer id, String name) {
        return systemVariableRepository.isUniqe(id, name).isEmpty();
    }

    @Transactional("coreTransactionManager")
    public void remove(SystemVariableDTO dto) {
        systemVariableRepository.deleteById(dto.getId());
    }

    public Optional<SystemVariable> findByName(String variableName) {
        return systemVariableRepository.findByName(variableName);
    }

    private static void bindFromDTO(SystemVariableDTO resource, SystemVariable existing) {
        existing.setName(resource.getName());
        existing.setDescription(resource.getDescription());
        existing.setJs(resource.getJs());
    }
}

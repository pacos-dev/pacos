
package org.pacos.core.component.variable.service;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.pacos.core.component.variable.domain.UserVariable;
import org.pacos.core.component.variable.domain.UserVariableCollection;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.dto.mapper.UserVariableCollectionMapper;
import org.pacos.core.component.variable.repository.UserVariableCollectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.pacos.core.component.variable.dto.mapper.UserVariableCollectionMapper.map;

@Service
@AllArgsConstructor
public class UserVariableCollectionService {

    private final UserVariableCollectionRepository collectionRepository;
    private final UserVariableService userVariableService;

    @Transactional("coreTransactionManager")
    public List<UserVariableCollection> getForUser(Integer userId) {
        List<UserVariableCollection> byUserIdOrderByName = collectionRepository.findByUserIdOrderByName(userId);
        if (byUserIdOrderByName.isEmpty()) {
            byUserIdOrderByName.add(createGlobalCollection(userId));
            byUserIdOrderByName.add(initializeDefaultCollection(userId));
        }
        if(byUserIdOrderByName.stream().noneMatch(UserVariableCollection::isGlobal)){
            byUserIdOrderByName.add(createGlobalCollection(userId));
        }
        return byUserIdOrderByName;
    }

    @Transactional("coreTransactionManager")
    public UserVariableCollectionDTO save(UserVariableCollectionDTO dto) {
        UserVariableCollection existing;
        if (dto.getId() != null) {
            existing = collectionRepository.findById(dto.getId()).orElse(new UserVariableCollection());
        } else {
            existing = new UserVariableCollection();
        }

        UserVariableCollectionMapper.bindFromDTO(dto, existing);
        return map(collectionRepository.saveAndFlush(existing));
    }

    @Transactional("coreTransactionManager")
    public void remove(UserVariableCollectionDTO dto) {
        userVariableService.deleteAllByCollectionId(dto.getId());
        collectionRepository.deleteById(dto.getId());
    }

    @Transactional("coreTransactionManager")
    public List<UserVariable> saveVariables(Integer collectionId, List<UserVariable> variables) {
        UserVariableCollection collection = collectionRepository.getReferenceById(collectionId);
        collection.getVariables().clear();
        variables.forEach(v -> v.setCollectionId(collectionId));
        collection.getVariables().addAll(variables);
        collectionRepository.save(collection);
        return collection.getVariables();
    }

    @Transactional("coreTransactionManager")
    public UserVariableCollection createGlobalCollection(int userId) {
        UserVariableCollection collection = new UserVariableCollection();
        collection.setUserId(userId);
        collection.setGlobal(true);
        collection.setName(UserVariableCollectionDTO.GLOBAL_NAME);
        return collectionRepository.save(collection);
    }

    @Transactional("coreTransactionManager")
    public UserVariableCollection clone(UserVariableCollectionDTO dto) {
        UserVariableCollection newCollection = new UserVariableCollection();
        UserVariableCollectionMapper.bindFromDTO(dto, newCollection);
        newCollection.setName("Clone " + dto.getName());
        newCollection = collectionRepository.save(newCollection);
        userVariableService.cloneForCollection(newCollection.getId(), dto);
        return newCollection;
    }

    public Optional<UserVariableCollection> findById(Integer collectionId) {
        return collectionRepository.findById(collectionId);
    }


    private UserVariableCollection initializeDefaultCollection(int userId) {
        UserVariableCollection collection = new UserVariableCollection();
        collection.setUserId(userId);
        collection.setGlobal(false);
        collection.setName("Example collection");
        collection = collectionRepository.save(collection);


        UserVariableDTO variableDTO = new UserVariableDTO();
        variableDTO.setName("exampleVariable");
        variableDTO.setEnabled(true);
        variableDTO.setInitialValue("example value");
        variableDTO.setCurrentValue("current example value");
        variableDTO.setCollectionId(collection.getId());

        UserVariableDTO variableDTO2 = new UserVariableDTO();
        variableDTO2.setName("exampleNumber");
        variableDTO2.setEnabled(true);
        variableDTO2.setInitialValue("11");
        variableDTO2.setCurrentValue("15");
        variableDTO2.setCollectionId(collection.getId());

        userVariableService.save(variableDTO);
        userVariableService.save(variableDTO2);

        return collection;
    }
}

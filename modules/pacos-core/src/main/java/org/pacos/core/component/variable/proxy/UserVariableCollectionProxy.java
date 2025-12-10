package org.pacos.core.component.variable.proxy;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.mapper.UserVariableCollectionMapper;
import org.pacos.core.component.variable.service.UserVariableCollectionService;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserVariableCollectionProxy {

    private final UserVariableCollectionService collectionService;

    public List<UserVariableCollectionDTO> loadUserCollections(Integer userId) {
        return collectionService.getForUser(userId).stream().map(UserVariableCollectionMapper::map)
                .toList();
    }

    public Optional<UserVariableCollectionDTO> loadCollection(Integer collectionId) {
        return collectionService.findById(collectionId).map(UserVariableCollectionMapper::map);
    }


    public UserVariableCollectionDTO createNewCollection(int userId) {
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder()
                .name("New collection")
                .userId(userId)
                .build();

        return collectionService.save(collectionDTO);
    }

    public void update(UserVariableCollectionDTO collectionDTO) {
        collectionService.save(collectionDTO);
    }

    public void removeCollection(UserVariableCollectionDTO collectionDTO) {
        collectionService.remove(collectionDTO);
    }

    public UserVariableCollectionDTO createGlobalCollection(int userId) {
        return UserVariableCollectionMapper.map(collectionService.createGlobalCollection(userId));
    }

    public UserVariableCollectionDTO cloneCollection(UserVariableCollectionDTO userVariableCollectionDTO) {
        return UserVariableCollectionMapper.map(collectionService.clone(userVariableCollectionDTO));
    }


}

package org.pacos.core.component.variable.proxy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.domain.UserVariableCollection;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.service.UserVariableCollectionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserVariableCollectionProxyTest {

    private final UserVariableCollectionService collectionServiceMock = mock(UserVariableCollectionService.class);
    private final UserVariableCollectionProxy collectionProxy = new UserVariableCollectionProxy(collectionServiceMock);

    @Test
    void whenLoadUserCollectionsThenReturnMappedList() {
        //when
        int userId = 1;
        UserVariableCollection collectionMock = mock(UserVariableCollection.class);
        List<UserVariableCollection> collections = List.of(collectionMock);
        when(collectionServiceMock.getForUser(userId)).thenReturn(collections);

        //when
        List<UserVariableCollectionDTO> result = collectionProxy.loadUserCollections(userId);

        //then
        assertNotNull(result);
        assertEquals(collections.size(), result.size());
    }

    @Test
    void whenLoadCollectionThenReturnOptionalMappedCollection() {
        //when
        int collectionId = 1;
        UserVariableCollection collectionMock = mock(UserVariableCollection.class);
        when(collectionServiceMock.findById(collectionId)).thenReturn(Optional.of(collectionMock));

        //when
        Optional<UserVariableCollectionDTO> result = collectionProxy.loadCollection(collectionId);

        //then
        assertTrue(result.isPresent());
    }

    @Test
    void whenCreateNewCollectionThenReturnNewCollection() {
        //when
        int userId = 1;
        UserVariableCollectionDTO newCollection = UserVariableCollectionDTO.builder()
                .name("New collection")
                .userId(userId)
                .build();
        when(collectionServiceMock.save(any(UserVariableCollectionDTO.class))).thenReturn(newCollection);

        //when
        UserVariableCollectionDTO result = collectionProxy.createNewCollection(userId);

        //then
        assertNotNull(result);
        assertEquals("New collection", result.getName());
        verify(collectionServiceMock).save(any(UserVariableCollectionDTO.class));
    }

    @Test
    void whenUpdateCollectionThenSaveCollection() {
        //when
        UserVariableCollectionDTO collectionDTO = mock(UserVariableCollectionDTO.class);

        //when
        collectionProxy.update(collectionDTO);

        //then
        verify(collectionServiceMock).save(collectionDTO);
    }

    @Test
    void whenRemoveCollectionThenInvokeServiceRemove() {
        //when
        UserVariableCollectionDTO collectionDTO = mock(UserVariableCollectionDTO.class);

        //when
        collectionProxy.removeCollection(collectionDTO);

        //then
        verify(collectionServiceMock).remove(collectionDTO);
    }

    @Test
    void whenCreateGlobalCollectionThenReturnMappedGlobalCollection() {
        //when
        int userId = 1;
        UserVariableCollection globalCollectionMock = mock(UserVariableCollection.class);
        when(collectionServiceMock.createGlobalCollection(userId)).thenReturn(globalCollectionMock);

        //when
        UserVariableCollectionDTO result = collectionProxy.createGlobalCollection(userId);

        //then
        assertNotNull(result);
        verify(collectionServiceMock).createGlobalCollection(userId);
    }

    @Test
    void whenCloneCollectionThenReturnMappedClonedCollection() {
        //when
        UserVariableCollectionDTO originalCollection = mock(UserVariableCollectionDTO.class);
        UserVariableCollection clonedCollectionMock = mock(UserVariableCollection.class);
        when(collectionServiceMock.clone(originalCollection)).thenReturn(clonedCollectionMock);

        //when
        UserVariableCollectionDTO result = collectionProxy.cloneCollection(originalCollection);

        //then
        assertNotNull(result);
        verify(collectionServiceMock).clone(originalCollection);
    }
}

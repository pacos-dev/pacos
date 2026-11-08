package org.pacos.core.component.credential.service;

import java.util.List;
import java.util.UUID;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.domain.CredentialFileEntity;
import org.pacos.core.component.credential.repository.CredentialFileRepository;
import org.pacos.core.component.credential.security.CredentialFileStorage;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

class CredentialFileManagerTest {

    @Test
    void whenOneFileCleanupFailsThenContinueDeletingRemainingFiles() {
        CredentialFileRepository repository = mock(CredentialFileRepository.class);
        CredentialFileStorage storage = mock(CredentialFileStorage.class);
        CredentialEntity credential = new CredentialEntity();
        credential.setId(UUID.randomUUID().toString());
        CredentialFileEntity first = file(credential, "first-ref");
        CredentialFileEntity second = file(credential, "second-ref");
        when(repository.findAllByCredential_Id(credential.getId())).thenReturn(List.of(first, second));
        doThrow(new IllegalStateException("file system unavailable")).when(storage).delete("first-ref");
        CredentialFileManager manager = new CredentialFileManager(repository, storage);

        TransactionSynchronizationManager.initSynchronization();
        try {
            manager.deleteFilesAfterCommit(credential);
            assertDoesNotThrow(() -> TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit));
            verify(storage).delete("first-ref");
            verify(storage).delete("second-ref");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void whenStoreFilesThenClearInputBuffer() {
        CredentialFileRepository repository = mock(CredentialFileRepository.class);
        CredentialFileStorage storage = mock(CredentialFileStorage.class);
        CredentialEntity credential = new CredentialEntity();
        credential.setId(UUID.randomUUID().toString());
        CredentialFileInput input = new CredentialFileInput("private.key", null, new byte[]{1, 2, 3});
        when(storage.store(any(), eq("privateKey"), any()))
                .thenReturn(new org.pacos.core.component.credential.security.StoredCredentialFile(
                        "stored-ref", 3, "checksum"));
        CredentialFileManager manager = new CredentialFileManager(repository, storage);

        manager.storeFiles(credential, Map.of("privateKey", input));

        assertArrayEquals(new byte[]{0, 0, 0}, input.content());
    }

    private static CredentialFileEntity file(CredentialEntity credential, String reference) {
        CredentialFileEntity file = new CredentialFileEntity();
        file.setCredential(credential);
        file.setStorageReference(reference);
        return file;
    }
}

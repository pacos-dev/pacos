package org.pacos.core.component.credential.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.pacos.base.credential.CredentialFileInput;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.domain.CredentialFileEntity;
import org.pacos.core.component.credential.repository.CredentialFileRepository;
import org.pacos.core.component.credential.security.CredentialFileStorage;
import org.pacos.core.component.credential.security.StoredCredentialFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Coordinates credential file metadata and encrypted file storage lifecycle. */
final class CredentialFileManager {

    private final CredentialFileRepository fileRepository;
    private final CredentialFileStorage fileStorage;

    CredentialFileManager(CredentialFileRepository fileRepository, CredentialFileStorage fileStorage) {
        this.fileRepository = fileRepository;
        this.fileStorage = fileStorage;
    }

    void storeFiles(CredentialEntity credential, Map<String, CredentialFileInput> files) {
        List<String> storedReferences = new ArrayList<>();
        try {
            files.forEach((fieldKey, file) -> saveFile(credential, fieldKey, file, storedReferences));
            registerAfterRollback(() -> deleteFiles(storedReferences));
        } catch (RuntimeException exception) {
            deleteFiles(storedReferences);
            throw exception;
        }
    }

    void updateFiles(
            CredentialEntity credential,
            Map<String, CredentialFileInput> filesToStore,
            java.util.Set<String> fieldsToRemove) {
        List<String> newlyStoredReferences = new ArrayList<>();
        List<String> replacedReferences = new ArrayList<>();
        try {
            filesToStore.forEach((fieldKey, file) -> {
                fileRepository.findByCredential_IdAndFieldKey(credential.getId(), fieldKey)
                        .ifPresent(existing -> replacedReferences.add(existing.getStorageReference()));
                saveFile(credential, fieldKey, file, newlyStoredReferences);
            });
            fieldsToRemove.forEach(fieldKey -> {
                fileRepository.findByCredential_IdAndFieldKey(credential.getId(), fieldKey)
                        .ifPresent(existing -> replacedReferences.add(existing.getStorageReference()));
                fileRepository.deleteByCredential_IdAndFieldKey(credential.getId(), fieldKey);
            });
            registerAfterRollback(() -> deleteFiles(newlyStoredReferences));
            registerAfterCommit(() -> deleteFiles(replacedReferences));
        } catch (RuntimeException exception) {
            deleteFiles(newlyStoredReferences);
            throw exception;
        }
    }

    void deleteFilesAfterCommit(CredentialEntity credential) {
        List<String> references = fileRepository.findAllByCredential_Id(credential.getId()).stream()
                .map(CredentialFileEntity::getStorageReference)
                .toList();
        registerAfterCommit(() -> deleteFiles(references));
    }

    Map<String, ResolvedCredentialImpl.FileResolver> createResolvers(UUID credentialId) {
        Map<String, ResolvedCredentialImpl.FileResolver> resolvers = new HashMap<>();
        for (CredentialFileEntity file : fileRepository.findAllByCredential_Id(credentialId.toString())) {
            String fieldKey = file.getFieldKey();
            String storageReference = file.getStorageReference();
            long size = file.getSize();
            String checksum = file.getChecksum();
            resolvers.put(fieldKey,
                    () -> fileStorage.load(credentialId, fieldKey, storageReference, size, checksum));
        }
        return resolvers;
    }

    private void saveFile(
            CredentialEntity credential,
            String fieldKey,
            CredentialFileInput file,
            List<String> storedReferences) {
        UUID credentialId = UUID.fromString(credential.getId());
        byte[] content = file.content();
        StoredCredentialFile stored;
        try {
            stored = fileStorage.store(credentialId, fieldKey, content);
        } finally {
            java.util.Arrays.fill(content, (byte) 0);
        }
        storedReferences.add(stored.storageReference());

        CredentialFileEntity entity = fileRepository
                .findByCredential_IdAndFieldKey(credential.getId(), fieldKey)
                .orElseGet(CredentialFileEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCredential(credential);
            entity.setFieldKey(fieldKey);
        }
        entity.setOriginalFilename(file.originalFilename());
        entity.setContentType(file.contentType());
        entity.setSize(stored.size());
        entity.setChecksum(stored.checksum());
        entity.setStorageReference(stored.storageReference());
        fileRepository.save(entity);
    }

    private void registerAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private void registerAfterRollback(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    action.run();
                }
            }
        });
    }

    private void deleteFiles(List<String> references) {
        references.forEach(fileStorage::delete);
    }
}

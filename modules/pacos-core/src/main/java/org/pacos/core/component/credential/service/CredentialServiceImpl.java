package org.pacos.core.component.credential.service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import org.pacos.base.utils.ObjectMapperUtils;
import org.pacos.base.credential.CredentialAccessContext;
import org.pacos.base.credential.CredentialAccessDescriptor;
import org.pacos.base.credential.CredentialAccessException;
import org.pacos.base.credential.CredentialAccessGrant;
import org.pacos.base.credential.CredentialAclPermission;
import org.pacos.base.credential.CredentialCreateRequest;
import org.pacos.base.credential.CredentialDescriptor;
import org.pacos.base.credential.CredentialFieldDefinition;
import org.pacos.base.credential.CredentialFieldType;
import org.pacos.base.credential.CredentialFileInput;
import org.pacos.base.credential.CredentialPrincipalType;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.credential.CredentialService;
import org.pacos.base.credential.CredentialTypeDefinition;
import org.pacos.base.credential.CredentialUpdateRequest;
import org.pacos.base.credential.CredentialVisibility;
import org.pacos.base.credential.ResolvedCredential;
import org.pacos.core.component.credential.domain.CredentialAccessEntity;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.domain.CredentialFileEntity;
import org.pacos.core.component.credential.domain.CredentialValueEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.credential.repository.CredentialFileRepository;
import org.pacos.core.component.credential.repository.CredentialRepository;
import org.pacos.core.component.credential.repository.CredentialValueRepository;
import org.pacos.core.component.credential.security.CredentialAccessPolicy;
import org.pacos.core.component.credential.security.CredentialFileStorage;
import org.pacos.core.component.credential.security.CredentialPermission;
import org.pacos.core.component.credential.security.StoredCredentialFile;
import org.pacos.core.component.credential.security.VaultCryptography;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepository credentialRepository;
    private final CredentialValueRepository valueRepository;
    private final CredentialFileRepository fileRepository;
    private final CredentialAccessRepository accessRepository;
    private final CredentialAccessPolicy accessPolicy;
    private final CredentialTypeRegistry typeRegistry;
    private final VaultCryptography cryptography;
    private final CredentialFileStorage fileStorage;

    public CredentialServiceImpl(
            CredentialRepository credentialRepository,
            CredentialValueRepository valueRepository,
            CredentialFileRepository fileRepository,
            CredentialAccessRepository accessRepository,
            CredentialAccessPolicy accessPolicy,
            CredentialTypeRegistry typeRegistry,
            VaultCryptography cryptography,
            CredentialFileStorage fileStorage) {
        this.credentialRepository = credentialRepository;
        this.valueRepository = valueRepository;
        this.fileRepository = fileRepository;
        this.accessRepository = accessRepository;
        this.accessPolicy = accessPolicy;
        this.typeRegistry = typeRegistry;
        this.cryptography = cryptography;
        this.fileStorage = fileStorage;
    }

    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public List<CredentialDescriptor> findAvailable(CredentialQuery query, CredentialAccessContext context) {
        if (context == null) {
            throw accessDenied();
        }
        CredentialQuery effectiveQuery = query == null ? CredentialQuery.all() : query;
        return credentialRepository.findAll().stream()
                .filter(credential -> accessPolicy.canView(credential, context))
                .filter(credential -> effectiveQuery.type() == null || effectiveQuery.type().equals(credential.getType()))
                .filter(credential -> matchesSearch(credential, effectiveQuery.searchTerm()))
                .map(this::toDescriptor)
                .toList();
    }

    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public CredentialDescriptor findById(CredentialReference reference, CredentialAccessContext context) {
        CredentialEntity credential = findCredential(reference);
        if (!accessPolicy.canView(credential, context)) {
            throw accessDenied();
        }
        return toDescriptor(credential);
    }

    @Override
    @Transactional("coreTransactionManager")
    public CredentialDescriptor create(CredentialCreateRequest request, CredentialAccessContext context) {
        requireUserContext(context);
        if (!accessPolicy.hasGlobalPermission(context.userId(), CredentialPermission.CREATE)
                || request != null && request.visibility() == CredentialVisibility.SHARED
                && !accessPolicy.hasGlobalPermission(context.userId(), CredentialPermission.MANAGE_ACCESS)) {
            throw accessDenied();
        }
        if (request == null) {
            throw new IllegalArgumentException("Credential request is required");
        }
        CredentialTypeDefinition definition = typeRegistry.getRequired(request.type());
        validateCreatePayload(definition, request.values(), request.files());

        Instant now = Instant.now();
        CredentialEntity credential = new CredentialEntity();
        credential.setId(UUID.randomUUID().toString());
        credential.setName(validateName(request.name()));
        credential.setDescription(validateDescription(request.description()));
        credential.setType(definition.key());
        credential.setOwnerUserId(context.userId());
        credential.setVisibility(request.visibility());
        credential.setCreatedAt(now);
        credential.setUpdatedAt(now);
        credentialRepository.save(credential);

        List<String> storedFiles = new ArrayList<>();
        try {
            storeValues(credential, definition, request.values());
            storeFiles(credential, request.files(), storedFiles);
            registerAfterRollback(() -> storedFiles.forEach(fileStorage::delete));
        } catch (RuntimeException e) {
            storedFiles.forEach(fileStorage::delete);
            throw e;
        }
        return toDescriptor(credential);
    }

    @Override
    @Transactional("coreTransactionManager")
    public CredentialDescriptor update(
            CredentialReference reference, CredentialUpdateRequest request, CredentialAccessContext context) {
        requireUserContext(context);
        CredentialEntity credential = findCredential(reference);
        if (request == null || !accessPolicy.hasGlobalPermission(context.userId(), CredentialPermission.UPDATE)
                || !accessPolicy.canManage(credential, context)) {
            throw accessDenied();
        }

        CredentialTypeDefinition definition = typeRegistry.getRequired(credential.getType());
        validateUpdatePayload(definition, request, credential);

        if (request.name() != null) {
            credential.setName(validateName(request.name()));
        }
        if (request.description() != null) {
            credential.setDescription(validateDescription(request.description()));
        }
        if (request.visibility() != null) {
            if (request.visibility() == CredentialVisibility.SHARED
                    && !accessPolicy.hasGlobalPermission(context.userId(), CredentialPermission.MANAGE_ACCESS)) {
                throw accessDenied();
            }
            credential.setVisibility(request.visibility());
        }
        credential.setUpdatedAt(Instant.now());

        List<String> newlyStoredFiles = new ArrayList<>();
        List<String> replacedFiles = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : request.values().entrySet()) {
                saveValue(credential, definitionField(definition, entry.getKey()), entry.getValue());
            }
            for (String fieldKey : request.removeFields()) {
                valueRepository.deleteByCredential_IdAndFieldKey(credential.getId(), fieldKey);
            }
            for (Map.Entry<String, CredentialFileInput> entry : request.files().entrySet()) {
                fileRepository.findByCredential_IdAndFieldKey(credential.getId(), entry.getKey())
                        .ifPresent(old -> replacedFiles.add(old.getStorageReference()));
                saveFile(credential, entry.getKey(), entry.getValue(), newlyStoredFiles);
            }
            for (String fieldKey : request.removeFiles()) {
                fileRepository.findByCredential_IdAndFieldKey(credential.getId(), fieldKey)
                        .ifPresent(old -> replacedFiles.add(old.getStorageReference()));
                fileRepository.deleteByCredential_IdAndFieldKey(credential.getId(), fieldKey);
            }
            registerAfterRollback(() -> newlyStoredFiles.forEach(fileStorage::delete));
            registerAfterCommit(() -> replacedFiles.forEach(fileStorage::delete));
        } catch (RuntimeException e) {
            newlyStoredFiles.forEach(fileStorage::delete);
            throw e;
        }
        credentialRepository.save(credential);
        return toDescriptor(credential);
    }

    @Override
    @Transactional("coreTransactionManager")
    public void delete(CredentialReference reference, CredentialAccessContext context) {
        requireUserContext(context);
        CredentialEntity credential = findCredential(reference);
        if (!accessPolicy.hasGlobalPermission(context.userId(), CredentialPermission.DELETE)
                || !accessPolicy.canManage(credential, context)) {
            throw accessDenied();
        }
        List<String> storageReferences = fileRepository.findAllByCredential_Id(credential.getId()).stream()
                .map(CredentialFileEntity::getStorageReference)
                .toList();
        credentialRepository.delete(credential);
        registerAfterCommit(() -> storageReferences.forEach(fileStorage::delete));
    }

    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public List<CredentialAccessDescriptor> findAccess(
            CredentialReference reference, CredentialAccessContext context) {
        CredentialEntity credential = findCredential(reference);
        requireManageAccess(credential, context);
        return accessRepository.findAllByCredential_Id(credential.getId()).stream()
                .map(access -> new CredentialAccessDescriptor(
                        access.getPrincipalType(), access.getPrincipalId(), access.getPermission()))
                .toList();
    }

    @Override
    @Transactional("coreTransactionManager")
    public void grantAccess(
            CredentialReference reference, CredentialAccessGrant grant, CredentialAccessContext context) {
        CredentialEntity credential = findCredential(reference);
        requireManageAccess(credential, context);
        if (grant == null) {
            throw new IllegalArgumentException("Credential access grant is required");
        }
        if (accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                credential.getId(), grant.principalType(), grant.principalId(), grant.permission()).isPresent()) {
            return;
        }
        CredentialAccessEntity access = new CredentialAccessEntity();
        access.setId(UUID.randomUUID().toString());
        access.setCredential(credential);
        access.setPrincipalType(grant.principalType());
        access.setPrincipalId(grant.principalId());
        access.setPermission(grant.permission());
        accessRepository.save(access);
    }

    @Override
    @Transactional("coreTransactionManager")
    public void revokeAccess(
            CredentialReference reference, CredentialAccessGrant grant, CredentialAccessContext context) {
        CredentialEntity credential = findCredential(reference);
        requireManageAccess(credential, context);
        if (grant == null) {
            throw new IllegalArgumentException("Credential access grant is required");
        }
        accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                credential.getId(), grant.principalType(), grant.principalId(), grant.permission())
                .ifPresent(accessRepository::delete);
    }

    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public ResolvedCredential resolve(CredentialReference reference, CredentialAccessContext context) {
        CredentialEntity credential = findCredential(reference);
        if (!accessPolicy.canUse(credential, context)) {
            throw accessDenied();
        }

        UUID credentialId = UUID.fromString(credential.getId());
        Map<String, Object> values = new HashMap<>();
        for (CredentialValueEntity value : valueRepository.findAllByCredential_Id(credential.getId())) {
            byte[] plaintext = cryptography.decrypt(value.getEncryptedValue(), associatedData(credentialId, value.getFieldKey(), value.getFieldType()));
            try {
                values.put(value.getFieldKey(), decode(value.getFieldType(), plaintext));
            } finally {
                java.util.Arrays.fill(plaintext, (byte) 0);
            }
        }

        Map<String, ResolvedCredentialImpl.FileResolver> files = new HashMap<>();
        for (CredentialFileEntity file : fileRepository.findAllByCredential_Id(credential.getId())) {
            String fieldKey = file.getFieldKey();
            String storageReference = file.getStorageReference();
            long size = file.getSize();
            String checksum = file.getChecksum();
            files.put(fieldKey, () -> fileStorage.load(credentialId, fieldKey, storageReference, size, checksum));
        }
        return new ResolvedCredentialImpl(values, files);
    }

    private void requireManageAccess(CredentialEntity credential, CredentialAccessContext context) {
        requireUserContext(context);
        if (!accessPolicy.canManage(credential, context)
                || !accessPolicy.hasGlobalPermission(context.userId(), CredentialPermission.MANAGE_ACCESS)) {
            throw accessDenied();
        }
    }

    private void requireUserContext(CredentialAccessContext context) {
        if (context == null || context.userId() == null || context.userId() == org.pacos.base.session.UserDTO.GUEST_ID
                || context.pluginId() != null) {
            throw accessDenied();
        }
    }

    private void validateCreatePayload(
            CredentialTypeDefinition definition,
            Map<String, Object> values,
            Map<String, CredentialFileInput> files) {
        validateKnownFields(definition, values.keySet(), files.keySet());
        for (CredentialFieldDefinition field : definition.fields()) {
            if (field.type() == CredentialFieldType.FILE) {
                if (field.required() && !files.containsKey(field.key())) {
                    throw new IllegalArgumentException("Required credential file is missing: " + field.key());
                }
            } else {
                Object value = values.get(field.key());
                if (field.required() && value == null) {
                    throw new IllegalArgumentException("Required credential field is missing: " + field.key());
                }
                if (value != null) {
                    validateValue(field, value);
                }
            }
        }
    }

    private void validateUpdatePayload(
            CredentialTypeDefinition definition, CredentialUpdateRequest request, CredentialEntity credential) {
        validateKnownFields(definition, request.values().keySet(), request.files().keySet());
        validateKnownFields(definition, request.removeFields(), request.removeFiles());

        Set<String> conflicts = new HashSet<>(request.values().keySet());
        conflicts.retainAll(request.removeFields());
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Credential fields cannot be updated and removed together: " + conflicts);
        }
        conflicts = new HashSet<>(request.files().keySet());
        conflicts.retainAll(request.removeFiles());
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Credential files cannot be replaced and removed together: " + conflicts);
        }

        for (Map.Entry<String, Object> entry : request.values().entrySet()) {
            validateValue(definitionField(definition, entry.getKey()), entry.getValue());
        }
        for (Map.Entry<String, CredentialFileInput> entry : request.files().entrySet()) {
            CredentialFieldDefinition field = definitionField(definition, entry.getKey());
            if (field.type() != CredentialFieldType.FILE) {
                throw new IllegalArgumentException("Credential field is not a file: " + field.key());
            }
            validateFile(entry.getValue());
        }
        for (String fieldKey : request.removeFields()) {
            CredentialFieldDefinition field = definitionField(definition, fieldKey);
            if (field.type() == CredentialFieldType.FILE || field.required()) {
                throw new IllegalArgumentException("Required or file field cannot be removed: " + fieldKey);
            }
        }
        for (String fieldKey : request.removeFiles()) {
            CredentialFieldDefinition field = definitionField(definition, fieldKey);
            if (field.type() != CredentialFieldType.FILE || field.required()) {
                throw new IllegalArgumentException("Required or non-file field cannot be removed: " + fieldKey);
            }
        }

        Set<String> existingValues = new HashSet<>();
        valueRepository.findAllByCredential_Id(credential.getId()).forEach(value -> existingValues.add(value.getFieldKey()));
        Set<String> existingFiles = new HashSet<>();
        fileRepository.findAllByCredential_Id(credential.getId()).forEach(file -> existingFiles.add(file.getFieldKey()));
        existingValues.removeAll(request.removeFields());
        existingValues.addAll(request.values().keySet());
        existingFiles.removeAll(request.removeFiles());
        existingFiles.addAll(request.files().keySet());

        for (CredentialFieldDefinition field : definition.fields()) {
            if (field.required() && field.type() == CredentialFieldType.FILE && !existingFiles.contains(field.key())) {
                throw new IllegalArgumentException("Required credential file is missing: " + field.key());
            }
            if (field.required() && field.type() != CredentialFieldType.FILE && !existingValues.contains(field.key())) {
                throw new IllegalArgumentException("Required credential field is missing: " + field.key());
            }
        }
    }

    private void validateKnownFields(
            CredentialTypeDefinition definition, Set<String> valueFields, Set<String> fileFields) {
        Set<String> known = definition.fields().stream().map(CredentialFieldDefinition::key)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> supplied = new HashSet<>(valueFields);
        supplied.addAll(fileFields);
        if (!known.containsAll(supplied)) {
            supplied.removeAll(known);
            throw new IllegalArgumentException("Unknown credential fields: " + supplied);
        }
        for (String key : valueFields) {
            if (definitionField(definition, key).type() == CredentialFieldType.FILE) {
                throw new IllegalArgumentException("Credential field must be supplied as a file: " + key);
            }
        }
        for (String key : fileFields) {
            if (definitionField(definition, key).type() != CredentialFieldType.FILE) {
                throw new IllegalArgumentException("Credential field does not accept a file: " + key);
            }
        }
    }

    private static void validateValue(CredentialFieldDefinition field, Object value) {
        boolean valid = switch (field.type()) {
            case TEXT -> value instanceof String;
            case SECRET -> value instanceof char[];
            case KEY_VALUE -> value instanceof Map<?, ?> map
                    && map.entrySet().stream().allMatch(entry -> entry.getKey() instanceof String
                    && entry.getValue() instanceof String);
            case BOOLEAN -> value instanceof Boolean;
            case NUMBER -> value instanceof Number;
            case FILE -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid value type for credential field: " + field.key());
        }
        if (field.required() && (value instanceof String text && text.isBlank()
                || value instanceof char[] secret && secret.length == 0
                || value instanceof Map<?, ?> map && map.isEmpty())) {
            throw new IllegalArgumentException("Required credential field cannot be empty: " + field.key());
        }
    }

    private static void validateFile(CredentialFileInput file) {
        if (file == null) {
            throw new IllegalArgumentException("Credential file is required");
        }
        if (file.size() == 0) {
            throw new IllegalArgumentException("Credential file cannot be empty");
        }
        if (file.originalFilename().length() > 255
                || file.contentType() != null && file.contentType().length() > 255) {
            throw new IllegalArgumentException("Credential file metadata is too long");
        }
    }

    private void storeValues(CredentialEntity credential, CredentialTypeDefinition definition, Map<String, Object> values) {
        values.forEach((key, value) -> saveValue(credential, definitionField(definition, key), value));
    }

    private void saveValue(CredentialEntity credential, CredentialFieldDefinition field, Object value) {
        byte[] plaintext = encode(field.type(), value);
        try {
            CredentialValueEntity entity = valueRepository
                    .findByCredential_IdAndFieldKey(credential.getId(), field.key())
                    .orElseGet(CredentialValueEntity::new);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID().toString());
                entity.setCredential(credential);
                entity.setFieldKey(field.key());
            }
            entity.setFieldType(field.type());
            entity.setEncryptedValue(cryptography.encrypt(plaintext,
                    associatedData(UUID.fromString(credential.getId()), field.key(), field.type())));
            valueRepository.save(entity);
        } finally {
            java.util.Arrays.fill(plaintext, (byte) 0);
            if (value instanceof char[] secret) {
                java.util.Arrays.fill(secret, '\0');
            }
        }
    }

    private void storeFiles(
            CredentialEntity credential,
            Map<String, CredentialFileInput> files,
            List<String> storedFiles) {
        files.forEach((key, file) -> saveFile(credential, key, file, storedFiles));
    }

    private void saveFile(
            CredentialEntity credential, String fieldKey, CredentialFileInput file, List<String> storedFiles) {
        validateFile(file);
        UUID credentialId = UUID.fromString(credential.getId());
        byte[] content = file.content();
        StoredCredentialFile stored;
        try {
            stored = fileStorage.store(credentialId, fieldKey, content);
        } finally {
            java.util.Arrays.fill(content, (byte) 0);
        }
        storedFiles.add(stored.storageReference());

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

    private byte[] encode(CredentialFieldType type, Object value) {
        try {
            return switch (type) {
                case TEXT -> ((String) value).getBytes(StandardCharsets.UTF_8);
                case SECRET -> encodeSecret((char[]) value);
                case KEY_VALUE, BOOLEAN, NUMBER -> ObjectMapperUtils.getMapper().writeValueAsBytes(value);
                case FILE -> throw new IllegalArgumentException("File fields must use Vault file storage");
            };
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot encode credential value", e);
        }
    }

    private static byte[] encodeSecret(char[] secret) {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(secret));
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    private Object decode(CredentialFieldType fieldType, byte[] plaintext) {
        try {
            return switch (fieldType) {
                case TEXT -> new String(plaintext, StandardCharsets.UTF_8);
                case SECRET -> decodeSecret(plaintext);
                case KEY_VALUE -> ObjectMapperUtils.getMapper().readValue(plaintext, new TypeReference<Map<String, String>>() {});
                case BOOLEAN -> ObjectMapperUtils.getMapper().readValue(plaintext, Boolean.class);
                case NUMBER -> ObjectMapperUtils.getMapper().readValue(plaintext, BigDecimal.class);
                case FILE -> throw new IllegalStateException("File values must be stored in Vault file storage");
            };
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot decode credential value", e);
        }
    }

    private static char[] decodeSecret(byte[] plaintext) {
        CharBuffer decoded = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(plaintext));
        char[] secret = new char[decoded.remaining()];
        decoded.get(secret);
        if (decoded.hasArray()) {
            java.util.Arrays.fill(decoded.array(), '\0');
        }
        return secret;
    }

    private static CredentialFieldDefinition definitionField(CredentialTypeDefinition definition, String key) {
        return definition.fields().stream()
                .filter(field -> field.key().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown credential field: " + key));
    }

    private CredentialEntity findCredential(CredentialReference reference) {
        if (reference == null || reference.id() == null) {
            throw accessDenied();
        }
        return credentialRepository.findById(reference.id().toString()).orElseThrow(CredentialServiceImpl::accessDenied);
    }

    private CredentialDescriptor toDescriptor(CredentialEntity credential) {
        return new CredentialDescriptor(
                UUID.fromString(credential.getId()),
                credential.getName(),
                credential.getDescription(),
                credential.getType(),
                credential.getOwnerUserId(),
                credential.getVisibility(),
                credential.getCreatedAt(),
                credential.getUpdatedAt());
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException("Credential name must contain between 1 and 255 characters");
        }
        return name.trim();
    }

    private static String validateDescription(String description) {
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Credential description cannot exceed 2000 characters");
        }
        return description;
    }

    private static boolean matchesSearch(CredentialEntity credential, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }
        String term = searchTerm.toLowerCase(Locale.ROOT);
        return credential.getName().toLowerCase(Locale.ROOT).contains(term)
                || credential.getType().toLowerCase(Locale.ROOT).contains(term)
                || credential.getDescription() != null && credential.getDescription().toLowerCase(Locale.ROOT).contains(term);
    }

    private static byte[] associatedData(UUID credentialId, String fieldKey, CredentialFieldType fieldType) {
        return (credentialId + ":" + fieldKey + ":" + fieldType.name() + ":value").getBytes(StandardCharsets.UTF_8);
    }

    private static CredentialAccessException accessDenied() {
        return new CredentialAccessException("Credential not found or access denied");
    }

    private static void registerAfterCommit(Runnable action) {
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

    private static void registerAfterRollback(Runnable action) {
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
}

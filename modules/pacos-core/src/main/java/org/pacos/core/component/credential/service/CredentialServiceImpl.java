package org.pacos.core.component.credential.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAccessDescriptor;
import org.pacos.base.credential.CredentialAccessException;
import org.pacos.core.component.credential.service.CredentialAccessGrant;
import org.pacos.core.component.credential.service.CredentialCreateRequest;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.core.component.credential.service.CredentialFieldDefinition;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.pacos.core.component.credential.service.CredentialUpdateRequest;
import org.pacos.core.component.credential.service.CredentialVisibility;
import org.pacos.base.credential.ResolvedCredential;
import org.pacos.base.session.UserDTO;
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
import org.pacos.core.component.credential.security.VaultCryptography;
import org.pacos.core.component.security.domain.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates credential use cases, authorization and persistence transactions.
 *
 * <p>Payload validation, value cryptography and file lifecycle are delegated to focused helpers.</p>
 *
 * <p>Guest sessions may find and resolve credentials granted to the GUEST role with ACCESS.
 * All mutating operations and access-grant administration require a non-guest user context.
 * A MANAGE grant cannot be assigned to the GUEST role; guests are never credential managers.</p>
 */
@Service
public class CredentialServiceImpl implements CredentialVaultService {

    private final CredentialRepository credentialRepository;
    private final CredentialValueRepository valueRepository;
    private final CredentialFileRepository fileRepository;
    private final CredentialAccessRepository accessRepository;
    private final CredentialAccessPolicy accessPolicy;
    private final CredentialTypeRegistry typeRegistry;
    private final CredentialPayloadValidator payloadValidator;
    private final CredentialValueCodec valueCodec;
    private final CredentialFileManager fileManager;

    /**
     * Creates the application service and its focused Vault collaborators.
     * @param credentialRepository credential metadata repository
     * @param valueRepository encrypted value repository
     * @param fileRepository file metadata repository
     * @param accessRepository access grant repository
     * @param accessPolicy authorization policy
     * @param typeRegistry supported credential type registry
     * @param cryptography Vault value cryptography
     * @param fileStorage encrypted file storage
     */
    public CredentialServiceImpl(
            CredentialRepository credentialRepository,
            CredentialValueRepository valueRepository,
            CredentialFileRepository fileRepository,
            CredentialAccessRepository accessRepository,
            CredentialAccessPolicy accessPolicy,
            CredentialTypeRegistry typeRegistry,
            VaultCryptography cryptography,
            CredentialFileStorage fileStorage,
            @Value("${pacos.credential-vault.max-file-size-bytes:10485760}") long maxFileSizeBytes) {
        this.credentialRepository = credentialRepository;
        this.valueRepository = valueRepository;
        this.fileRepository = fileRepository;
        this.accessRepository = accessRepository;
        this.accessPolicy = accessPolicy;
        this.typeRegistry = typeRegistry;
        this.payloadValidator = new CredentialPayloadValidator(valueRepository, fileRepository, maxFileSizeBytes);
        this.valueCodec = new CredentialValueCodec(cryptography);
        this.fileManager = new CredentialFileManager(fileRepository, fileStorage);
    }

    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public List<CredentialTypeDefinition> getAvailableTypes() {
        return List.copyOf(typeRegistry.getAll());
    }

    /**
     * Finds credentials visible to the supplied access context.
     * @param query optional type and text filters
     * @param context access context used for authorization
     * @return visible credential descriptors
     */
    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public List<CredentialAdminDescriptor> findAvailable(CredentialQuery query, CredentialAccessContext context) {
        if (context == null) {
            throw accessDenied();
        }
        CredentialQuery effectiveQuery = query == null ? CredentialQuery.all() : query;
        CredentialAccessPolicy.CredentialViewAccess viewAccess = accessPolicy.loadViewAccess(context);
        List<CredentialEntity> credentials = credentialRepository.findAll().stream()
                .filter(credential -> accessPolicy.canView(credential, context, viewAccess))
                .filter(credential -> effectiveQuery.type() == null || effectiveQuery.type().equals(credential.getType()))
                .filter(credential -> matchesSearch(credential, effectiveQuery.searchTerm()))
                .toList();
        return toDescriptors(credentials, context, viewAccess);
    }

    /**
     * Finds a credential by reference after checking visibility.
     * @param reference stable credential reference
     * @param context access context used for authorization
     * @return credential metadata without secret values
     */
    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public CredentialAdminDescriptor findById(CredentialReference reference, CredentialAccessContext context) {
        CredentialEntity credential = findCredential(reference);
        if (!accessPolicy.canView(credential, context)) {
            throw accessDenied();
        }
        return toDescriptor(credential, accessPolicy.canManage(credential, context));
    }

    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public Map<String, Object> findNonSensitiveValues(
            CredentialReference reference, CredentialAccessContext context) {
        requireUserContext(context);
        CredentialEntity credential = findCredential(reference);
        if (!accessPolicy.canManage(credential, context)) {
            throw accessDenied();
        }
        Set<String> readableFields = typeRegistry.getRequired(credential.getType()).fields().stream()
                .filter(field -> !field.sensitive() && field.type() != org.pacos.core.component.credential.service.CredentialFieldType.FILE)
                .map(CredentialFieldDefinition::key)
                .collect(Collectors.toSet());
        Map<String, Object> values = new java.util.HashMap<>();
        UUID credentialId = UUID.fromString(credential.getId());
        for (CredentialValueEntity value : valueRepository.findAllByCredential_Id(credential.getId())) {
            if (readableFields.contains(value.getFieldKey())) {
                values.put(value.getFieldKey(), valueCodec.decrypt(credentialId, value));
            }
        }
        return Map.copyOf(values);
    }

    /**
     * Creates a credential and persists its encrypted values and files.
     * @param request credential metadata and initial payload
     * @param context authenticated user context
     * @return descriptor of the created credential
     */
    @Override
    @Transactional("coreTransactionManager")
    public CredentialAdminDescriptor create(CredentialCreateRequest request, CredentialAccessContext context) {
        if (request == null) {
            throw new IllegalArgumentException("Credential request is required");
        }
        try {
            requireUserContext(context);
            CredentialTypeDefinition definition = typeRegistry.getRequired(request.type());
            payloadValidator.validateCreate(definition, request.values(), request.files());

            CredentialEntity credential = createCredential(request, definition, context.userId());
            credentialRepository.save(credential);
            storeValues(credential, definition, request.values());
            fileManager.storeFiles(credential, request.files());
            return toDescriptor(credential, true);
        } finally {
            request.files().values().forEach(CredentialFileInput::clear);
        }
    }

    /**
     * Applies a partial update to credential metadata, values and files.
     * @param reference stable credential reference
     * @param request partial update payload
     * @param context authenticated user context
     * @return descriptor of the updated credential
     */
    @Override
    @Transactional("coreTransactionManager")
    public CredentialAdminDescriptor update(
            CredentialReference reference, CredentialUpdateRequest request, CredentialAccessContext context) {
        if (request == null) {
            throw accessDenied();
        }
        try {
            requireUserContext(context);
            CredentialEntity credential = findCredential(reference);
            if (!accessPolicy.canManage(credential, context)) {
                throw accessDenied();
            }

            CredentialTypeDefinition definition = typeRegistry.getRequired(credential.getType());
            payloadValidator.validateUpdate(definition, request, credential);

            updateMetadata(credential, request);

            updateValues(credential, definition, request);
            for (String fieldKey : request.removeFields()) {
                valueRepository.deleteByCredential_IdAndFieldKey(credential.getId(), fieldKey);
            }
            fileManager.updateFiles(credential, request.files(), request.removeFiles());
            credentialRepository.save(credential);
            return toDescriptor(credential, true);
        } finally {
            request.files().values().forEach(CredentialFileInput::clear);
        }
    }

    /**
     * Deletes a credential and schedules its stored files for deletion after commit.
     * @param reference stable credential reference
     * @param context authenticated user context
     */
    @Override
    @Transactional("coreTransactionManager")
    public void delete(CredentialReference reference, CredentialAccessContext context) {
        requireUserContext(context);
        CredentialEntity credential = findCredential(reference);
        if (!accessPolicy.canManage(credential, context)) {
            throw accessDenied();
        }
        fileManager.deleteFilesAfterCommit(credential);
        credentialRepository.delete(credential);
    }

    /**
     * Lists access grants assigned to a credential.
     * @param reference stable credential reference
     * @param context authenticated user context
     * @return credential access grants
     */
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

    /**
     * Grants a credential permission to a group or plugin.
     *
     * <p>Plugins may receive ACCESS only. The GUEST group may also receive ACCESS, but cannot
     * receive MANAGE because guest sessions are never allowed to manage credentials.</p>
     *
     * @param reference stable credential reference
     * @param grant principal and permission to grant
     * @param context authenticated user context
     */
    @Override
    @Transactional("coreTransactionManager")
    public void grantAccess(
            CredentialReference reference, CredentialAccessGrant grant, CredentialAccessContext context) {
        CredentialEntity credential = findCredential(reference);
        requireManageAccess(credential, context);
        if (grant == null) {
            throw new IllegalArgumentException("Credential access grant is required");
        }
        if ((grant.principalType() == CredentialPrincipalType.PLUGIN
                || grant.principalType() == CredentialPrincipalType.APPLICATION)
                && grant.permission() == CredentialAclPermission.MANAGE) {
            throw new IllegalArgumentException("Plugin and application principals can only receive ACCESS permission");
        }
        if (grant.principalType() == CredentialPrincipalType.APPLICATION
                && !CredentialAccessContext.PACOS_APPLICATION_ID.equals(grant.principalId())) {
            throw new IllegalArgumentException("Unsupported application principal");
        }
        if (grant.principalType() == org.pacos.core.component.credential.service.CredentialPrincipalType.GROUP
                && Integer.toString(Role.GUEST_ROLE).equals(grant.principalId())
                && grant.permission() == org.pacos.core.component.credential.service.CredentialAclPermission.MANAGE) {
            throw new IllegalArgumentException("The GUEST group can only receive ACCESS permission");
        }
        if (accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                credential.getId(), grant.principalType(), grant.principalId(), grant.permission()).isPresent()) {
            throw new IllegalArgumentException("This configuration already exists.");
        }
        CredentialAccessEntity access = new CredentialAccessEntity();
        access.setId(UUID.randomUUID().toString());
        access.setCredential(credential);
        access.setPrincipalType(grant.principalType());
        access.setPrincipalId(grant.principalId());
        access.setPermission(grant.permission());
        accessRepository.save(access);
    }

    /**
     * Revokes a credential permission from a group or plugin.
     * @param reference stable credential reference
     * @param grant principal and permission to revoke
     * @param context authenticated user context
     */
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

    /**
     * Resolves decrypted credential data for an authorized caller.
     * <p>The returned value owns sensitive buffers and must be closed by the caller.</p>
     * @param reference stable credential reference
     * @param context access context used for authorization
     * @return closeable view of the decrypted credential data
     */
    @Override
    @Transactional(value = "coreTransactionManager", readOnly = true)
    public ResolvedCredential resolve(CredentialReference reference, CredentialAccessContext context) {
        CredentialEntity credential = findCredential(reference);
        if (!accessPolicy.canUse(credential, context)) {
            throw accessDenied();
        }

        UUID credentialId = UUID.fromString(credential.getId());
        Map<String, Object> values = new java.util.HashMap<>();
        try {
            for (CredentialValueEntity value : valueRepository.findAllByCredential_Id(credential.getId())) {
                values.put(value.getFieldKey(), valueCodec.decrypt(credentialId, value));
            }
            return new ResolvedCredentialImpl(values, fileManager.createResolvers(credentialId));
        } catch (RuntimeException exception) {
            values.values().stream()
                    .filter(char[].class::isInstance)
                    .map(char[].class::cast)
                    .forEach(secret -> java.util.Arrays.fill(secret, '\0'));
            throw exception;
        }
    }

    private CredentialEntity createCredential(
            CredentialCreateRequest request, CredentialTypeDefinition definition, int ownerUserId) {
        Instant now = Instant.now();
        CredentialEntity credential = new CredentialEntity();
        credential.setId(UUID.randomUUID().toString());
        credential.setName(validateName(request.name()));
        credential.setDescription(validateDescription(request.description()));
        credential.setType(definition.key());
        credential.setOwnerUserId(ownerUserId);
        credential.setVisibility(request.visibility());
        credential.setCreatedAt(now);
        credential.setUpdatedAt(now);
        return credential;
    }

    private void updateMetadata(CredentialEntity credential, CredentialUpdateRequest request) {
        if (request.name() != null) {
            credential.setName(validateName(request.name()));
        }
        if (request.description() != null) {
            credential.setDescription(validateDescription(request.description()));
        }
        if (request.visibility() != null) {
            credential.setVisibility(request.visibility());
        }
        credential.setUpdatedAt(Instant.now());
    }

    private void requireManageAccess(CredentialEntity credential, CredentialAccessContext context) {
        requireUserContext(context);
        if (!accessPolicy.canManage(credential, context)) {
            throw accessDenied();
        }
    }

    /**
     * Requires an authenticated non-guest user for credential mutations and ACL administration.
     * Guest sessions are limited to viewing and resolving credentials made visible by normal visibility
     * rules or GUEST ACCESS.
     *
     * @param context access context to validate
     */
    private void requireUserContext(CredentialAccessContext context) {
        if (context == null || context.userId() == null || context.userId() == UserDTO.GUEST_ID
                || context.pluginId() != null) {
            throw accessDenied();
        }
    }

    private void storeValues(
            CredentialEntity credential, CredentialTypeDefinition definition, Map<String, Object> values) {
        values.forEach((key, value) -> saveValue(credential, definitionField(definition, key), value));
    }

    private void updateValues(
            CredentialEntity credential, CredentialTypeDefinition definition, CredentialUpdateRequest request) {
        request.values().forEach((key, value) -> saveValue(credential, definitionField(definition, key), value));
    }

    private void saveValue(CredentialEntity credential, CredentialFieldDefinition field, Object value) {
        CredentialValueEntity entity = valueRepository
                .findByCredential_IdAndFieldKey(credential.getId(), field.key())
                .orElseGet(CredentialValueEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCredential(credential);
            entity.setFieldKey(field.key());
        }
        entity.setFieldType(field.type());
        entity.setEncryptedValue(valueCodec.encrypt(
                UUID.fromString(credential.getId()), field.key(), field.type(), value));
        valueRepository.save(entity);
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
        return credentialRepository.findById(reference.id().toString())
                .orElseThrow(CredentialServiceImpl::accessDenied);
    }

    private CredentialAdminDescriptor toDescriptor(CredentialEntity credential, boolean manageable) {
        Set<String> configuredFields = valueRepository.findAllByCredential_Id(credential.getId()).stream()
                .map(CredentialValueEntity::getFieldKey)
                .collect(Collectors.toSet());
        Set<String> configuredFiles = fileRepository.findAllByCredential_Id(credential.getId()).stream()
                .map(CredentialFileEntity::getFieldKey)
                .collect(Collectors.toSet());
        return toDescriptor(credential, configuredFields, configuredFiles, manageable);
    }

    private List<CredentialAdminDescriptor> toDescriptors(
            List<CredentialEntity> credentials,
            CredentialAccessContext context,
            CredentialAccessPolicy.CredentialViewAccess viewAccess) {
        if (credentials.isEmpty()) {
            return List.of();
        }
        List<String> credentialIds = credentials.stream().map(CredentialEntity::getId).toList();
        Map<String, Set<String>> configuredFields = valueRepository.findAllByCredential_IdIn(credentialIds).stream()
                .collect(Collectors.groupingBy(value -> value.getCredential().getId(),
                        Collectors.mapping(CredentialValueEntity::getFieldKey, Collectors.toSet())));
        Map<String, Set<String>> configuredFiles = fileRepository.findAllByCredential_IdIn(credentialIds).stream()
                .collect(Collectors.groupingBy(file -> file.getCredential().getId(),
                        Collectors.mapping(CredentialFileEntity::getFieldKey, Collectors.toSet())));
        return credentials.stream()
                .map(credential -> toDescriptor(
                        credential,
                        configuredFields.getOrDefault(credential.getId(), Set.of()),
                        configuredFiles.getOrDefault(credential.getId(), Set.of()),
                        accessPolicy.canManage(credential, context, viewAccess)))
                .toList();
    }

    private CredentialAdminDescriptor toDescriptor(
            CredentialEntity credential,
            Set<String> configuredFields,
            Set<String> configuredFiles,
            boolean manageable) {
        return new CredentialAdminDescriptor(
                UUID.fromString(credential.getId()),
                credential.getName(),
                credential.getDescription(),
                credential.getType(),
                credential.getOwnerUserId(),
                credential.getVisibility(),
                credential.getCreatedAt(),
                credential.getUpdatedAt(),
                configuredFields,
                configuredFiles,
                manageable);
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
                || credential.getDescription() != null
                && credential.getDescription().toLowerCase(Locale.ROOT).contains(term);
    }

    private static CredentialAccessException accessDenied() {
        return new CredentialAccessException("Credential not found or access denied");
    }

}

package org.pacos.core.component.credential.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAccessDescriptor;
import org.pacos.base.credential.CredentialAccessException;
import org.pacos.core.component.credential.service.CredentialAccessGrant;
import org.pacos.core.component.credential.service.CredentialAclPermission;
import org.pacos.core.component.credential.service.CredentialCreateRequest;
import org.pacos.core.component.credential.service.CredentialFieldDefinition;
import org.pacos.core.component.credential.service.CredentialFieldType;
import org.pacos.core.component.credential.service.CredentialFileInput;
import org.pacos.core.component.credential.service.CredentialPrincipalType;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.pacos.core.component.credential.service.CredentialUpdateRequest;
import org.pacos.core.component.credential.service.CredentialVisibility;
import org.pacos.base.credential.ResolvedCredential;
import org.pacos.base.utils.ObjectMapperUtils;
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
import org.pacos.core.component.credential.security.StoredCredentialFile;
import org.pacos.core.component.credential.security.VaultCryptography;
import org.pacos.core.component.credential.security.VaultKeyProvider;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplCoverageTest {

    private static final int USER_ID = 7;

    private static final CredentialAccessContext USER = CredentialAccessContext.forUser(USER_ID);

    @Mock
    private CredentialRepository credentialRepository;
    @Mock
    private CredentialValueRepository valueRepository;
    @Mock
    private CredentialFileRepository fileRepository;
    @Mock
    private CredentialAccessRepository accessRepository;
    @Mock
    private CredentialAccessPolicy accessPolicy;
    @Mock
    private CredentialFileStorage fileStorage;
    @Mock
    private VaultKeyProvider keyProvider;

    private CredentialServiceImpl service;
    private VaultCryptography cryptography;

    @BeforeEach
    void setUp() {
        cryptography = new VaultCryptography(keyProvider);
        service = new CredentialServiceImpl(
                credentialRepository,
                valueRepository,
                fileRepository,
                accessRepository,
                accessPolicy,
                new CredentialTypeRegistry(),
                cryptography,
                fileStorage,
                10 * 1024 * 1024L);
    }

    private void stubMasterKey() {
        when(keyProvider.getMasterKey()).thenReturn(new SecretKeySpec(new byte[32], "AES"));
    }

    @Test
    void whenGetAvailableTypesThenReturnBuiltInDefinitions() {
        assertEquals(6, service.getAvailableTypes().size());
    }

    @Test
    void whenFindNonSensitiveValuesThenExcludeSecretAndFileFields() {
        stubMasterKey();
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "basic-auth", "Production API", null);
        CredentialValueCodec codec = new CredentialValueCodec(cryptography);
        CredentialValueEntity username = new CredentialValueEntity();
        username.setCredential(credential);
        username.setFieldKey("username");
        username.setFieldType(CredentialFieldType.TEXT);
        username.setEncryptedValue(codec.encrypt(id, "username", CredentialFieldType.TEXT, "api-user"));
        CredentialValueEntity password = new CredentialValueEntity();
        password.setCredential(credential);
        password.setFieldKey("password");
        password.setFieldType(CredentialFieldType.SECRET);
        password.setEncryptedValue(codec.encrypt(id, "password", CredentialFieldType.SECRET, "secret".toCharArray()));
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        when(valueRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(username, password));

        Map<String, Object> values = service.findNonSensitiveValues(new CredentialReference(id), USER);

        assertEquals(Map.of("username", "api-user"), values);
    }

    @Test
    void whenFindByIdThenReturnConfiguredFieldAndFileNames() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "client-certificate", "Production certificate", null);
        CredentialValueEntity value = new CredentialValueEntity();
        value.setCredential(credential);
        value.setFieldKey("password");
        CredentialFileEntity file = new CredentialFileEntity();
        file.setCredential(credential);
        file.setFieldKey("privateKey");
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canView(credential, USER)).thenReturn(true);
        when(valueRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(value));
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(file));

        var descriptor = service.findById(new CredentialReference(id), USER);

        assertEquals(Set.of("password"), descriptor.configuredFields());
        assertEquals(Set.of("privateKey"), descriptor.configuredFiles());
    }

    @Test
    void whenFindAvailableThenFilterByVisibilityTypeAndSearchTerm() {
        CredentialEntity visible = credential(UUID.randomUUID(), "bearer-token", "Production API", "Live token");
        CredentialEntity hidden = credential(UUID.randomUUID(), "bearer-token", "Production API", "Live token");
        CredentialEntity wrongType = credential(UUID.randomUUID(), "basic-auth", "Production API", "Live token");
        when(credentialRepository.findAll()).thenReturn(List.of(visible, hidden, wrongType));
        CredentialAccessPolicy.CredentialViewAccess viewAccess = null;
        when(accessPolicy.loadViewAccess(USER)).thenReturn(viewAccess);
        when(accessPolicy.canView(visible, USER, viewAccess)).thenReturn(true);
        when(accessPolicy.canManage(visible, USER, viewAccess)).thenReturn(true);
        when(accessPolicy.canView(hidden, USER, viewAccess)).thenReturn(false);
        when(accessPolicy.canView(wrongType, USER, viewAccess)).thenReturn(true);

        List<?> result = service.findAvailable(new CredentialQuery("bearer-token", "LIVE"), USER);

        assertEquals(1, result.size());
        org.pacos.core.component.credential.service.CredentialAdminDescriptor descriptor =
                (org.pacos.core.component.credential.service.CredentialAdminDescriptor) result.getFirst();
        assertEquals(visible.getId(), descriptor.id().toString());
        assertTrue(descriptor.manageable());
    }

    @Test
    void whenGrantManagePermissionToPluginThenThrowException() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "Production API", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.MANAGE),
                USER));

        verify(accessRepository, never()).save(any());
    }

    @Test
    void whenGrantManagePermissionToApplicationThenThrowException() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "Production API", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(
                        CredentialPrincipalType.APPLICATION, "pacos", CredentialAclPermission.MANAGE),
                USER));

        verify(accessRepository, never()).save(any());
    }

    @Test
    void whenCreateCredentialWithOversizedFileThenThrowException() {
        CredentialPayloadValidator validator = new CredentialPayloadValidator(valueRepository, fileRepository, 2);
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "test", "Test", null,
                List.of(new CredentialFieldDefinition(
                        "certificate", "Certificate", CredentialFieldType.FILE, false, false, false)));
        CredentialFileInput file = new CredentialFileInput("certificate.pem", "application/x-pem-file",
                new byte[]{1, 2, 3});

        assertThrows(IllegalArgumentException.class,
                () -> validator.validateCreate(definition, Map.of(), Map.of("certificate", file)));
    }

    @Test
    void whenFindAvailableWithNullContextThenThrowAccessDenied() {
        assertThrows(CredentialAccessException.class, () -> service.findAvailable(null, null));
    }

    @Test
    void whenFindAvailableWithNullQueryThenReturnAllVisibleCredentials() {
        CredentialEntity visible = credential(UUID.randomUUID(), "bearer-token", "Production API", null);
        when(credentialRepository.findAll()).thenReturn(List.of(visible));
        CredentialAccessPolicy.CredentialViewAccess viewAccess = null;
        when(accessPolicy.loadViewAccess(USER)).thenReturn(viewAccess);
        when(accessPolicy.canView(visible, USER, viewAccess)).thenReturn(true);

        assertEquals(1, service.findAvailable(null, USER).size());
    }

    @Test
    void whenFindByIdThenReturnCredentialDescriptor() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "Production API", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canView(credential, USER)).thenReturn(true);

        var result = service.findById(new CredentialReference(id), USER);

        assertEquals(id, result.id());
        assertEquals("Production API", result.name());
    }

    @Test
    void whenFindByIdWithoutPermissionThenThrowAccessDenied() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "Production API", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));

        assertThrows(CredentialAccessException.class, () -> service.findById(new CredentialReference(id), USER));
    }

    @Test
    void whenCreateWithBasicAuthThenPersistTextAndEncryptedSecret() {
        stubMasterKey();
        char[] password = "top-secret".toCharArray();
        CredentialCreateRequest request = new CredentialCreateRequest(
                "  Production API  ",
                "Primary credentials",
                "basic-auth",
                CredentialVisibility.PRIVATE,
                Map.of("username", "api-user", "password", password),
                Map.of());

        var descriptor = service.create(request, USER);

        ArgumentCaptor<CredentialEntity> credentialCaptor = ArgumentCaptor.forClass(CredentialEntity.class);
        ArgumentCaptor<CredentialValueEntity> valueCaptor = ArgumentCaptor.forClass(CredentialValueEntity.class);
        verify(credentialRepository).save(credentialCaptor.capture());
        verify(valueRepository, org.mockito.Mockito.times(2)).save(valueCaptor.capture());
        assertEquals("Production API", descriptor.name());
        assertEquals(USER_ID, descriptor.ownerUserId());
        assertEquals("Production API", credentialCaptor.getValue().getName());
        assertArrayEquals(new char[password.length], password);
        assertEquals(2, valueCaptor.getAllValues().size());
        assertTrue(valueCaptor.getAllValues().stream().allMatch(value -> value.getEncryptedValue().length > 0));
    }

    @Test
    void whenCreateSharedCredentialThenAllow() {
        stubMasterKey();
        CredentialCreateRequest request = new CredentialCreateRequest(
                "Shared API", null, "bearer-token", CredentialVisibility.SHARED,
                Map.of("token", "secret".toCharArray()), Map.of());

        var descriptor = service.create(request, USER);

        assertEquals(CredentialVisibility.SHARED, descriptor.visibility());
        verify(credentialRepository).save(any());
    }

    @Test
    void whenCreateWithNullRequestThenThrowIllegalArgumentException() {

        assertThrows(IllegalArgumentException.class, () -> service.create(null, USER));
    }

    @Test
    void whenCreateWithPluginContextThenThrowAccessDenied() {
        assertThrows(CredentialAccessException.class,
                () -> service.create(new CredentialCreateRequest(
                        "API", null, "bearer-token", CredentialVisibility.PRIVATE,
                        Map.of("token", "secret".toCharArray()), Map.of()),
                        CredentialAccessContext.forPlugin("apinity")));
    }

    @Test
    void whenCreateWithUnknownFieldThenThrowIllegalArgumentException() {
        CredentialCreateRequest request = new CredentialCreateRequest(
                "API", null, "bearer-token", CredentialVisibility.PRIVATE,
                Map.of("token", "secret".toCharArray(), "unknown", "value"), Map.of());

        assertThrows(IllegalArgumentException.class, () -> service.create(request, USER));
    }

    @Test
    void whenCreateWithOAuthScopesThenSerializeKeyValueField() {
        stubMasterKey();
        Map<String, String> scopes = Map.of("read", "true", "write", "false");
        CredentialCreateRequest request = new CredentialCreateRequest(
                "OAuth client", null, "oauth2-client", CredentialVisibility.PRIVATE,
                Map.of("clientId", "client", "clientSecret", "secret".toCharArray(),
                        "tokenUrl", "https://example.test/token", "scopes", scopes),
                Map.of());

        service.create(request, USER);

        ArgumentCaptor<CredentialValueEntity> captor = ArgumentCaptor.forClass(CredentialValueEntity.class);
        verify(valueRepository, org.mockito.Mockito.times(4)).save(captor.capture());
        CredentialValueEntity scopesEntity = captor.getAllValues().stream()
                .filter(value -> "scopes".equals(value.getFieldKey()))
                .findFirst().orElseThrow();
        assertEquals(CredentialFieldType.KEY_VALUE, scopesEntity.getFieldType());
        byte[] plaintext = cryptography.decrypt(scopesEntity.getEncryptedValue(),
                (scopesEntity.getCredential().getId() + ":scopes:KEY_VALUE:value").getBytes(StandardCharsets.UTF_8));
        assertEquals(scopes, ObjectMapperUtils.getMapper().readValue(plaintext,
                new tools.jackson.core.type.TypeReference<Map<String, String>>() {}));
    }

    @Test
    void whenCreateClientCertificateThenStoreBothFiles() {
        when(fileStorage.store(any(), eq("certificate"), any()))
                .thenReturn(new StoredCredentialFile("certificate-ref", 4, "certificate-checksum"));
        when(fileStorage.store(any(), eq("privateKey"), any()))
                .thenReturn(new StoredCredentialFile("private-key-ref", 5, "key-checksum"));
        Map<String, CredentialFileInput> files = new LinkedHashMap<>();
        files.put("certificate", new CredentialFileInput("client.crt", "application/pem-certificate-chain",
                new byte[]{1, 2, 3, 4}));
        files.put("privateKey", new CredentialFileInput("client.key", "application/pkcs8",
                new byte[]{5, 6, 7, 8, 9}));
        CredentialCreateRequest request = new CredentialCreateRequest(
                "mTLS", null, "client-certificate", CredentialVisibility.PRIVATE,
                Map.of(), files);

        service.create(request, USER);

        ArgumentCaptor<CredentialFileEntity> captor = ArgumentCaptor.forClass(CredentialFileEntity.class);
        verify(fileRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(2, captor.getAllValues().size());
        assertEquals(List.of("certificate-ref", "private-key-ref"),
                captor.getAllValues().stream().map(CredentialFileEntity::getStorageReference).sorted().toList());
    }

    @Test
    void whenUpdateThenRemoveOptionalOAuthScopeAndUpdateMetadata() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "oauth2-client", "Old name", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        when(valueRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(
                valueMetadata(credential, "clientId"), valueMetadata(credential, "clientSecret"),
                valueMetadata(credential, "tokenUrl"), valueMetadata(credential, "scopes")));
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of());
        CredentialUpdateRequest request = new CredentialUpdateRequest(
                "New name", "Updated description", null, Map.of(), Map.of(),
                java.util.Set.of("scopes"), java.util.Set.of());

        var result = service.update(new CredentialReference(id), request, USER);

        assertEquals("New name", result.name());
        verify(valueRepository).deleteByCredential_IdAndFieldKey(id.toString(), "scopes");
        verify(credentialRepository).save(credential);
    }

    @Test
    void whenUpdateRequiredFieldIsRemovedThenThrowIllegalArgumentException() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "API", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        CredentialUpdateRequest request = new CredentialUpdateRequest(
                null, null, null, Map.of(), Map.of(),
                java.util.Set.of("token"), java.util.Set.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.update(new CredentialReference(id), request, USER));
    }

    @Test
    void whenUpdateSameFieldAndRemoveItThenThrowIllegalArgumentException() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "API", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        CredentialUpdateRequest request = new CredentialUpdateRequest(
                null, null, null, Map.of("token", "new-token".toCharArray()), Map.of(),
                java.util.Set.of("token"), java.util.Set.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.update(new CredentialReference(id), request, USER));
    }

    @Test
    void whenUpdateSharedVisibilityThenAllowForCredentialManager() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "API", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        CredentialUpdateRequest request = new CredentialUpdateRequest(
                null, null, CredentialVisibility.SHARED, Map.of(), Map.of(),
                java.util.Set.of(), java.util.Set.of());
        when(valueRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(
                valueMetadata(credential, "token")));
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of());

        service.update(new CredentialReference(id), request, USER);
    }

    @Test
    void whenUpdateClientCertificateThenReplaceFileAndDeleteOldStorage() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "client-certificate", "mTLS", null);
        CredentialFileEntity oldCertificate = fileMetadata(credential, "certificate", "old-certificate-ref");
        CredentialFileEntity privateKey = fileMetadata(credential, "privateKey", "private-key-ref");
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        when(valueRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of());
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(oldCertificate, privateKey));
        when(fileRepository.findByCredential_IdAndFieldKey(id.toString(), "certificate"))
                .thenReturn(Optional.of(oldCertificate));
        when(fileStorage.store(eq(id), eq("certificate"), any()))
                .thenReturn(new StoredCredentialFile("new-certificate-ref", 4, "new-checksum"));
        CredentialUpdateRequest request = new CredentialUpdateRequest(
                null, null, null, Map.of(),
                Map.of("certificate", new CredentialFileInput("new.crt", "application/pem-certificate-chain",
                        new byte[]{1, 2, 3, 4})),
                java.util.Set.of(), java.util.Set.of());

        service.update(new CredentialReference(id), request, USER);

        assertEquals("new-certificate-ref", oldCertificate.getStorageReference());
        verify(fileStorage).delete("old-certificate-ref");
        verify(fileRepository).save(oldCertificate);
    }

    @Test
    void whenUpdateWithPluginContextThenThrowAccessDenied() {
        UUID id = UUID.randomUUID();
        assertThrows(CredentialAccessException.class,
                () -> service.update(new CredentialReference(id),
                        new CredentialUpdateRequest(null, null, null, Map.of(), Map.of(),
                                java.util.Set.of(), java.util.Set.of()),
                        CredentialAccessContext.forPlugin("apinity")));
    }

    @Test
    void whenDeleteThenRemoveCredentialAndStoredFiles() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "client-certificate", "mTLS", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(
                fileMetadata(credential, "certificate", "certificate-ref"),
                fileMetadata(credential, "privateKey", "private-key-ref")));

        service.delete(new CredentialReference(id), USER);

        verify(credentialRepository).delete(credential);
        verify(fileStorage).delete("certificate-ref");
        verify(fileStorage).delete("private-key-ref");
    }

    @Test
    void whenFindAccessThenReturnCredentialGrants() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "API", null);
        CredentialAccessEntity access = new CredentialAccessEntity();
        access.setPrincipalType(CredentialPrincipalType.PLUGIN);
        access.setPrincipalId("apinity");
        access.setPermission(CredentialAclPermission.ACCESS);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        when(accessRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(access));

        List<CredentialAccessDescriptor> result = service.findAccess(new CredentialReference(id), USER);

        assertEquals(1, result.size());
        assertEquals(CredentialPrincipalType.PLUGIN, result.getFirst().principalType());
        assertEquals("apinity", result.getFirst().principalId());
        assertEquals(CredentialAclPermission.ACCESS, result.getFirst().permission());
    }

    @Test
    void whenGrantExistingAccessThenDoNotPersistDuplicate() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "API", null);
        CredentialAccessGrant grant = new CredentialAccessGrant(
                CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS);
        CredentialAccessEntity existing = new CredentialAccessEntity();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        when(accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                id.toString(), grant.principalType(), grant.principalId(), grant.permission()))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.grantAccess(new CredentialReference(id), grant, USER));

        verify(accessRepository, never()).save(any());
    }

    @Test
    void whenRevokeExistingAccessThenDeleteGrant() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "API", null);
        CredentialAccessGrant grant = new CredentialAccessGrant(
                CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS);
        CredentialAccessEntity existing = new CredentialAccessEntity();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        when(accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                id.toString(), grant.principalType(), grant.principalId(), grant.permission()))
                .thenReturn(Optional.of(existing));

        service.revokeAccess(new CredentialReference(id), grant, USER);

        verify(accessRepository).delete(existing);
    }

    @Test
    void whenResolveThenDecodeValuesAndLoadFile() throws Exception {
        stubMasterKey();
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "oauth2-client", "OAuth", null);
        List<CredentialValueEntity> values = List.of(
                encryptedValue(credential, "clientId", CredentialFieldType.TEXT, "client-123"),
                encryptedValue(credential, "clientSecret", CredentialFieldType.SECRET, "secret-value".toCharArray()),
                encryptedValue(credential, "scopes", CredentialFieldType.KEY_VALUE, Map.of("read", "true")),
                encryptedValue(credential, "enabled", CredentialFieldType.BOOLEAN, true),
                encryptedValue(credential, "timeout", CredentialFieldType.NUMBER, new BigDecimal("12.50")));
        CredentialFileEntity file = fileMetadata(credential, "certificate", "certificate-ref");
        file.setSize(4);
        file.setChecksum("checksum");
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canUse(credential, USER)).thenReturn(true);
        when(valueRepository.findAllByCredential_Id(id.toString())).thenReturn(values);
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(file));
        when(fileStorage.load(id, "certificate", "certificate-ref", 4, "checksum"))
                .thenReturn(new byte[]{1, 2, 3, 4});

        try (ResolvedCredential resolved = service.resolve(new CredentialReference(id), USER)) {
            assertEquals("client-123", resolved.getText("clientId"));
            assertArrayEquals("secret-value".toCharArray(), resolved.getSecret("clientSecret"));
            assertEquals(Map.of("read", "true"), resolved.getKeyValues("scopes"));
            assertNull(resolved.getText("missing"));
            assertEquals(4, resolved.openFile("certificate").readAllBytes().length);
        }
    }

    @Test
    void whenResolveWithoutUsePermissionThenThrowAccessDenied() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "bearer-token", "API", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));

        assertThrows(CredentialAccessException.class,
                () -> service.resolve(new CredentialReference(id), USER));
    }

    @Test
    void whenResolveMissingCredentialThenThrowAccessDenied() {
        UUID id = UUID.randomUUID();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.empty());

        assertThrows(CredentialAccessException.class,
                () -> service.resolve(new CredentialReference(id), USER));
    }

    @Test
    void whenCreateWithActiveTransactionThenDeleteStoredFilesAfterRollback() {
        when(fileStorage.store(any(), eq("certificate"), any()))
                .thenReturn(new StoredCredentialFile("certificate-ref", 1, "certificate-checksum"));
        when(fileStorage.store(any(), eq("privateKey"), any()))
                .thenReturn(new StoredCredentialFile("private-key-ref", 1, "key-checksum"));
        CredentialCreateRequest request = new CredentialCreateRequest(
                "mTLS", null, "client-certificate", CredentialVisibility.PRIVATE, Map.of(),
                Map.of(
                        "certificate", new CredentialFileInput("client.crt", null, new byte[]{1}),
                        "privateKey", new CredentialFileInput("client.key", null, new byte[]{2})));

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.create(request, USER);

            verify(fileStorage, never()).delete(any());
            TransactionSynchronizationManager.getSynchronizations().forEach(
                    synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

            verify(fileStorage).delete("certificate-ref");
            verify(fileStorage).delete("private-key-ref");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void whenDeleteWithActiveTransactionThenDeleteStoredFilesAfterCommit() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id, "client-certificate", "mTLS", null);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, USER)).thenReturn(true);
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(List.of(
                fileMetadata(credential, "certificate", "certificate-ref")));

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.delete(new CredentialReference(id), USER);

            verify(fileStorage, never()).delete(any());
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);

            verify(fileStorage).delete("certificate-ref");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private static CredentialEntity credential(UUID id, String type, String name, String description) {
        CredentialEntity credential = new CredentialEntity();
        credential.setId(id.toString());
        credential.setOwnerUserId(USER_ID);
        credential.setType(type);
        credential.setName(name);
        credential.setDescription(description);
        credential.setVisibility(CredentialVisibility.PRIVATE);
        return credential;
    }

    private static CredentialValueEntity valueMetadata(CredentialEntity credential, String fieldKey) {
        CredentialValueEntity value = new CredentialValueEntity();
        value.setCredential(credential);
        value.setFieldKey(fieldKey);
        return value;
    }

    private static CredentialFileEntity fileMetadata(
            CredentialEntity credential, String fieldKey, String storageReference) {
        CredentialFileEntity file = new CredentialFileEntity();
        file.setCredential(credential);
        file.setFieldKey(fieldKey);
        file.setStorageReference(storageReference);
        return file;
    }

    private CredentialValueEntity encryptedValue(
            CredentialEntity credential, String fieldKey, CredentialFieldType type, Object value) {
        byte[] plaintext = switch (type) {
            case TEXT -> ((String) value).getBytes(StandardCharsets.UTF_8);
            case SECRET -> new String((char[]) value).getBytes(StandardCharsets.UTF_8);
            case KEY_VALUE, BOOLEAN, NUMBER -> ObjectMapperUtils.getMapper().writeValueAsBytes(value);
            case FILE -> throw new IllegalArgumentException();
        };
        CredentialValueEntity entity = valueMetadata(credential, fieldKey);
        entity.setFieldType(type);
        entity.setEncryptedValue(cryptography.encrypt(plaintext,
                (credential.getId() + ":" + fieldKey + ":" + type.name() + ":value")
                        .getBytes(StandardCharsets.UTF_8)));
        return entity;
    }
}

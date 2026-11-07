package org.pacos.core.component.credential.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.credential.CredentialAccessContext;
import org.pacos.base.credential.CredentialAccessGrant;
import org.pacos.base.credential.CredentialAclPermission;
import org.pacos.base.credential.CredentialCreateRequest;
import org.pacos.base.credential.CredentialPrincipalType;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.credential.CredentialVisibility;
import org.pacos.base.credential.CredentialUpdateRequest;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.domain.CredentialValueEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.credential.repository.CredentialFileRepository;
import org.pacos.core.component.credential.repository.CredentialRepository;
import org.pacos.core.component.credential.repository.CredentialValueRepository;
import org.pacos.core.component.credential.security.CredentialAccessPolicy;
import org.pacos.core.component.credential.security.CredentialFileStorage;
import org.pacos.core.component.credential.security.CredentialPermission;
import org.pacos.core.component.credential.security.VaultCryptography;
import org.pacos.core.component.credential.security.VaultKeyProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {

    private static final int USER_ID = 7;

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

    private CredentialServiceImpl credentialService;
    private VaultCryptography cryptography;

    @BeforeEach
    void setUp() {
        VaultKeyProvider keyProvider = new VaultKeyProvider(Base64.getEncoder().encodeToString(new byte[32]));
        cryptography = new VaultCryptography(keyProvider);
        credentialService = new CredentialServiceImpl(
                credentialRepository,
                valueRepository,
                fileRepository,
                accessRepository,
                accessPolicy,
                new CredentialTypeRegistry(),
                cryptography,
                fileStorage,
                new ObjectMapper());
    }

    @Test
    void whenCreateCredentialThenSecretIsEncryptedBeforePersistence() {
        when(accessPolicy.hasGlobalPermission(USER_ID, CredentialPermission.CREATE)).thenReturn(true);
        when(valueRepository.findByCredential_IdAndFieldKey(any(), eq("token"))).thenReturn(Optional.empty());
        char[] secret = "sensitive-token".toCharArray();
        CredentialCreateRequest request = new CredentialCreateRequest(
                "Production API",
                null,
                "bearer-token",
                CredentialVisibility.PRIVATE,
                Map.of("token", secret),
                Map.of());

        credentialService.create(request, CredentialAccessContext.forUser(USER_ID));

        ArgumentCaptor<CredentialValueEntity> captor = ArgumentCaptor.forClass(CredentialValueEntity.class);
        verify(valueRepository).save(captor.capture());
        CredentialValueEntity persisted = captor.getValue();
        byte[] encrypted = persisted.getEncryptedValue();
        byte[] plaintext = cryptography.decrypt(encrypted,
                (persisted.getCredential().getId() + ":token:SECRET:value").getBytes(StandardCharsets.UTF_8));

        assertNotEquals("sensitive-token", new String(encrypted, StandardCharsets.UTF_8));
        assertArrayEquals("sensitive-token".getBytes(StandardCharsets.UTF_8), plaintext);
    }

    @Test
    void whenUpdateCredentialThenOnlySuppliedSecretIsReplaced() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = new CredentialEntity();
        credential.setId(id.toString());
        credential.setOwnerUserId(USER_ID);
        credential.setType("bearer-token");
        CredentialValueEntity existingValue = new CredentialValueEntity();
        existingValue.setId(UUID.randomUUID().toString());
        existingValue.setCredential(credential);
        existingValue.setFieldKey("token");
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.hasGlobalPermission(USER_ID, CredentialPermission.UPDATE)).thenReturn(true);
        when(accessPolicy.canManage(credential, CredentialAccessContext.forUser(USER_ID))).thenReturn(true);
        when(valueRepository.findAllByCredential_Id(id.toString())).thenReturn(java.util.List.of(existingValue));
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(java.util.List.of());
        when(valueRepository.findByCredential_IdAndFieldKey(id.toString(), "token")).thenReturn(Optional.of(existingValue));

        CredentialUpdateRequest request = new CredentialUpdateRequest(
                null,
                null,
                null,
                Map.of("token", "rotated-token".toCharArray()),
                Map.of(),
                java.util.Set.of(),
                java.util.Set.of());

        credentialService.update(new CredentialReference(id), request, CredentialAccessContext.forUser(USER_ID));

        ArgumentCaptor<CredentialValueEntity> captor = ArgumentCaptor.forClass(CredentialValueEntity.class);
        verify(valueRepository).save(captor.capture());
        byte[] plaintext = cryptography.decrypt(captor.getValue().getEncryptedValue(),
                (id + ":token:SECRET:value").getBytes(StandardCharsets.UTF_8));

        assertArrayEquals("rotated-token".getBytes(StandardCharsets.UTF_8), plaintext);
    }

    @Test
    void whenDeleteCredentialThenDeleteOwnedCredential() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = new CredentialEntity();
        credential.setId(id.toString());
        credential.setOwnerUserId(USER_ID);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.hasGlobalPermission(USER_ID, CredentialPermission.DELETE)).thenReturn(true);
        when(accessPolicy.canManage(credential, CredentialAccessContext.forUser(USER_ID))).thenReturn(true);
        when(fileRepository.findAllByCredential_Id(id.toString())).thenReturn(java.util.List.of());

        credentialService.delete(new CredentialReference(id), CredentialAccessContext.forUser(USER_ID));

        verify(credentialRepository).delete(credential);
    }

    @Test
    void whenCreateCredentialWithoutRequiredFieldThenThrowException() {
        when(accessPolicy.hasGlobalPermission(USER_ID, CredentialPermission.CREATE)).thenReturn(true);
        CredentialCreateRequest request = new CredentialCreateRequest(
                "Production API",
                null,
                "bearer-token",
                CredentialVisibility.PRIVATE,
                Map.of(),
                Map.of());

        assertThrows(IllegalArgumentException.class,
                () -> credentialService.create(request, CredentialAccessContext.forUser(USER_ID)));
    }

    @Test
    void whenGrantAccessThenPersistGrant() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = new CredentialEntity();
        credential.setId(id.toString());
        credential.setOwnerUserId(USER_ID);
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, CredentialAccessContext.forUser(USER_ID))).thenReturn(true);
        when(accessPolicy.hasGlobalPermission(USER_ID, CredentialPermission.MANAGE_ACCESS)).thenReturn(true);
        when(accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                id.toString(), CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.USE))
                .thenReturn(Optional.empty());

        credentialService.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.USE),
                CredentialAccessContext.forUser(USER_ID));

        ArgumentCaptor<org.pacos.core.component.credential.domain.CredentialAccessEntity> captor =
                ArgumentCaptor.forClass(org.pacos.core.component.credential.domain.CredentialAccessEntity.class);
        verify(accessRepository).save(captor.capture());
        org.pacos.core.component.credential.domain.CredentialAccessEntity saved = captor.getValue();

        assertEquals(id.toString(), saved.getCredential().getId());
        assertEquals(CredentialPrincipalType.PLUGIN, saved.getPrincipalType());
        assertEquals("apinity", saved.getPrincipalId());
        assertEquals(CredentialAclPermission.USE, saved.getPermission());
    }
}

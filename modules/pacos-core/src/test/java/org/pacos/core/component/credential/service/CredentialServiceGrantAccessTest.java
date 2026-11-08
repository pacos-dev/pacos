package org.pacos.core.component.credential.service;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAccessGrant;
import org.pacos.core.component.credential.service.CredentialAclPermission;
import org.pacos.core.component.credential.service.CredentialPrincipalType;
import org.pacos.base.credential.CredentialReference;
import org.pacos.core.component.credential.service.CredentialVisibility;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.credential.repository.CredentialFileRepository;
import org.pacos.core.component.credential.repository.CredentialRepository;
import org.pacos.core.component.credential.repository.CredentialValueRepository;
import org.pacos.core.component.credential.security.CredentialAccessPolicy;
import org.pacos.core.component.credential.security.CredentialFileStorage;
import org.pacos.core.component.credential.security.VaultCryptography;
import org.pacos.core.component.credential.security.VaultKeyProvider;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialServiceGrantAccessTest {

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
    @Mock
    private VaultKeyProvider keyProvider;

    @Test
    void whenGrantManagePermissionToPluginThenThrowException() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);
        CredentialServiceImpl service = service();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, user)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.MANAGE),
                user));

        verify(accessRepository, never()).save(any());
    }

    @Test
    void whenGrantAccessToGuestGroupThenSaveGrant() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);
        CredentialServiceImpl service = service();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, user)).thenReturn(true);
        when(accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                id.toString(), CredentialPrincipalType.GROUP, "1", CredentialAclPermission.ACCESS))
                .thenReturn(Optional.empty());

        service.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(CredentialPrincipalType.GROUP, "1", CredentialAclPermission.ACCESS),
                user);

        verify(accessRepository).save(any());
    }

    @Test
    void whenGrantManagePermissionToGuestGroupThenThrowException() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);
        CredentialServiceImpl service = service();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, user)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(CredentialPrincipalType.GROUP, "1", CredentialAclPermission.MANAGE),
                user));

        verify(accessRepository, never()).save(any());
    }

    @Test
    void whenGuestGrantsCredentialAccessThenThrowAccessDenied() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id);
        CredentialAccessContext guest = CredentialAccessContext.forUser(1);
        CredentialServiceImpl service = service();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));

        assertThrows(org.pacos.base.credential.CredentialAccessException.class, () -> service.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(CredentialPrincipalType.GROUP, "3", CredentialAclPermission.ACCESS),
                guest));

        verify(accessRepository, never()).save(any());
    }

    @Test
    void whenGrantDuplicateAccessThenThrowConfigurationAlreadyExists() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);
        CredentialServiceImpl service = service();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, user)).thenReturn(true);
        when(accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                id.toString(), CredentialPrincipalType.GROUP, "3", CredentialAclPermission.ACCESS))
                .thenReturn(Optional.of(new org.pacos.core.component.credential.domain.CredentialAccessEntity()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(CredentialPrincipalType.GROUP, "3", CredentialAclPermission.ACCESS),
                user));

        org.junit.jupiter.api.Assertions.assertEquals("This configuration already exists.", exception.getMessage());
        verify(accessRepository, never()).save(any());
    }

    @Test
    void whenGrantManagePermissionToGroupThenSaveGrant() {
        UUID id = UUID.randomUUID();
        CredentialEntity credential = credential(id);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);
        CredentialServiceImpl service = service();
        when(credentialRepository.findById(id.toString())).thenReturn(Optional.of(credential));
        when(accessPolicy.canManage(credential, user)).thenReturn(true);
        when(accessRepository.findByCredential_IdAndPrincipalTypeAndPrincipalIdAndPermission(
                id.toString(), CredentialPrincipalType.GROUP, "3", CredentialAclPermission.MANAGE))
                .thenReturn(Optional.empty());

        service.grantAccess(
                new CredentialReference(id),
                new CredentialAccessGrant(CredentialPrincipalType.GROUP, "3", CredentialAclPermission.MANAGE),
                user);

        verify(accessRepository).save(any());
    }

    private CredentialServiceImpl service() {
        return new CredentialServiceImpl(
                credentialRepository, valueRepository, fileRepository, accessRepository, accessPolicy,
                new CredentialTypeRegistry(), new VaultCryptography(keyProvider), fileStorage, 1024);
    }

    private static CredentialEntity credential(UUID id) {
        CredentialEntity credential = new CredentialEntity();
        credential.setId(id.toString());
        credential.setOwnerUserId(20);
        credential.setType("bearer-token");
        credential.setName("API");
        credential.setVisibility(CredentialVisibility.PRIVATE);
        return credential;
    }
}

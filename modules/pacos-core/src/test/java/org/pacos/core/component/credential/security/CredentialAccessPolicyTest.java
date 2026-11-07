package org.pacos.core.component.credential.security;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.credential.CredentialAccessContext;
import org.pacos.base.credential.CredentialAclPermission;
import org.pacos.base.credential.CredentialPrincipalType;
import org.pacos.base.credential.CredentialVisibility;
import org.pacos.base.security.PermissionName;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.security.service.PermissionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialAccessPolicyTest {

    private static final int USER_ID = 10;

    @Mock
    private CredentialAccessRepository accessRepository;
    @Mock
    private PermissionService permissionService;

    private CredentialAccessPolicy accessPolicy;
    private CredentialEntity credential;

    @BeforeEach
    void setUp() {
        accessPolicy = new CredentialAccessPolicy(accessRepository, permissionService);
        credential = new CredentialEntity();
        credential.setId("credential-id");
        credential.setOwnerUserId(20);
        credential.setVisibility(CredentialVisibility.PRIVATE);
    }

    @Test
    void whenGlobalManageAccessPermissionThenCannotManageCredential() {
        when(permissionService.loadUserPermission(USER_ID))
                .thenReturn(Set.of(new PermissionName(CredentialPermission.MANAGE_ACCESS.getKey())));

        assertFalse(accessPolicy.canManage(credential, CredentialAccessContext.forUser(USER_ID)));
        assertTrue(accessPolicy.canManageAccess(credential, CredentialAccessContext.forUser(USER_ID)));
    }

    @Test
    void whenGuestSessionRequestsCredentialThenCannotView() {
        assertFalse(accessPolicy.canView(credential, CredentialAccessContext.forUser(1)));
    }

    @Test
    void whenPrivateCredentialAndUserHasNoGrantThenCannotView() {
        assertFalse(accessPolicy.canView(credential, CredentialAccessContext.forUser(USER_ID)));
    }

    @Test
    void whenSharedCredentialAndUserHasViewPermissionThenCanView() {
        credential.setVisibility(CredentialVisibility.SHARED);
        when(permissionService.loadUserPermission(USER_ID))
                .thenReturn(Set.of(new PermissionName(CredentialPermission.VIEW.getKey())));

        assertTrue(accessPolicy.canView(credential, CredentialAccessContext.forUser(USER_ID)));
    }

    @Test
    void whenSharedCredentialAndUserHasNoUsePermissionThenCannotUse() {
        credential.setVisibility(CredentialVisibility.SHARED);
        when(permissionService.loadUserPermission(USER_ID))
                .thenReturn(Set.of(new PermissionName(CredentialPermission.VIEW.getKey())));

        assertFalse(accessPolicy.canUse(credential, CredentialAccessContext.forUser(USER_ID)));
    }

    @Test
    void whenPluginHasUseGrantThenCanViewCredentialMetadata() {
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.VIEW))
                .thenReturn(false);
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.USE))
                .thenReturn(true);

        assertTrue(accessPolicy.canView(credential, CredentialAccessContext.forPlugin("apinity")));
    }

    @Test
    void whenPluginHasUseGrantThenCanUseCredential() {
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.USE))
                .thenReturn(true);

        assertTrue(accessPolicy.canUse(credential, CredentialAccessContext.forPlugin("apinity")));
    }
}

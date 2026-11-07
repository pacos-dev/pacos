package org.pacos.core.component.credential.security;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.credential.CredentialAccessContext;
import org.pacos.base.credential.CredentialAclPermission;
import org.pacos.base.credential.CredentialPrincipalType;
import org.pacos.base.credential.CredentialVisibility;
import org.pacos.base.security.PermissionName;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.security.service.PermissionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialAccessPolicyCoverageTest {

    private static final int USER_ID = 10;

    @Mock
    private CredentialAccessRepository accessRepository;
    @Mock
    private PermissionService permissionService;

    private CredentialAccessPolicy policy;
    private CredentialEntity credential;

    @BeforeEach
    void setUp() {
        policy = new CredentialAccessPolicy(accessRepository, permissionService);
        credential = new CredentialEntity();
        credential.setId("credential-id");
        credential.setOwnerUserId(20);
        credential.setVisibility(CredentialVisibility.PRIVATE);
    }

    @Test
    void whenOwnerRequestsPrivateCredentialThenCanViewAndManage() {
        CredentialAccessContext owner = CredentialAccessContext.forUser(20);

        assertTrue(policy.canView(credential, owner));
        assertTrue(policy.canManage(credential, owner));
    }

    @Test
    void whenAdminRequestsCredentialThenCanViewUseAndManage() {
        CredentialAccessContext admin = CredentialAccessContext.forUser(UserDTO.ADMIN_ID);

        assertTrue(policy.canView(credential, admin));
        assertTrue(policy.canUse(credential, admin));
        assertTrue(policy.canManage(credential, admin));
    }

    @Test
    void whenUserHasUseGrantAndGlobalUsePermissionThenCanViewAndUse() {
        when(permissionService.loadUserPermission(USER_ID))
                .thenReturn(Set.of(new PermissionName(CredentialPermission.USE.getKey())));
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.USER, Integer.toString(USER_ID), CredentialAclPermission.USE))
                .thenReturn(true);

        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);

        assertTrue(policy.canView(credential, user));
        assertTrue(policy.canUse(credential, user));
    }

    @Test
    void whenSharedCredentialAndUserHasGlobalUsePermissionThenCanUse() {
        credential.setVisibility(CredentialVisibility.SHARED);
        when(permissionService.loadUserPermission(USER_ID))
                .thenReturn(Set.of(new PermissionName(CredentialPermission.USE.getKey())));

        assertTrue(policy.canUse(credential, CredentialAccessContext.forUser(USER_ID)));
    }

    @Test
    void whenUserHasManageGrantThenCanManage() {
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.USER, Integer.toString(USER_ID), CredentialAclPermission.MANAGE))
                .thenReturn(true);

        assertTrue(policy.canManage(credential, CredentialAccessContext.forUser(USER_ID)));
    }

    @Test
    void whenPluginHasViewGrantThenCanView() {
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.VIEW))
                .thenReturn(true);

        assertTrue(policy.canView(credential, CredentialAccessContext.forPlugin("apinity")));
    }

    @Test
    void whenPluginHasManageGrantThenCanViewAndUse() {
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.MANAGE))
                .thenReturn(true);

        CredentialAccessContext plugin = CredentialAccessContext.forPlugin("apinity");

        assertTrue(policy.canView(credential, plugin));
        assertTrue(policy.canUse(credential, plugin));
    }

    @Test
    void whenPluginActsForUserWithoutGlobalViewPermissionThenCannotView() {
        CredentialAccessContext plugin = CredentialAccessContext.forUserAndPlugin(USER_ID, "apinity");

        assertFalse(policy.canView(credential, plugin));
        verify(permissionService).loadUserPermission(USER_ID);
    }

    @Test
    void whenPluginActsForUserWithoutGlobalUsePermissionThenCannotUse() {
        CredentialAccessContext plugin = CredentialAccessContext.forUserAndPlugin(USER_ID, "apinity");

        assertFalse(policy.canUse(credential, plugin));
        verify(permissionService).loadUserPermission(USER_ID);
    }

    @Test
    void whenGuestOrPluginTriesToManageThenCannotManage() {
        assertFalse(policy.canManage(credential, CredentialAccessContext.forUser(UserDTO.GUEST_ID)));
        assertFalse(policy.canManage(credential, CredentialAccessContext.forPlugin("apinity")));
    }

    @Test
    void whenUserHasGlobalManageAccessPermissionThenCanManage() {
        when(permissionService.loadUserPermission(USER_ID))
                .thenReturn(Set.of(new PermissionName(CredentialPermission.MANAGE_ACCESS.getKey())));

        assertTrue(policy.canManage(credential, CredentialAccessContext.forUser(USER_ID)));
    }
}

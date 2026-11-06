package org.pacos.core.component.credential.security;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAclPermission;
import org.pacos.core.component.credential.service.CredentialPrincipalType;
import org.pacos.core.component.credential.service.CredentialVisibility;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.credential.domain.CredentialAccessEntity;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.user.repository.UserRepository;
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
    private UserRepository userRepository;

    private CredentialAccessPolicy policy;
    private CredentialEntity credential;

    @BeforeEach
    void setUp() {
        policy = new CredentialAccessPolicy(accessRepository, userRepository);
        credential = new CredentialEntity();
        credential.setId("credential-id");
        credential.setOwnerUserId(20);
        credential.setVisibility(CredentialVisibility.PRIVATE);
    }

    @Test
    void whenOwnerRequestsPrivateCredentialThenCanViewUseAndManage() {
        CredentialAccessContext owner = CredentialAccessContext.forUser(20);

        assertTrue(policy.canView(credential, owner));
        assertTrue(policy.canUse(credential, owner));
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
    void whenSharedCredentialThenCanUseWithoutGlobalPermission() {
        credential.setVisibility(CredentialVisibility.SHARED);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);

        assertTrue(policy.canView(credential, user));
        assertTrue(policy.canUse(credential, user));
    }

    @Test
    void whenPluginHasAccessGrantThenCanViewAndUse() {
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS))
                .thenReturn(true);
        CredentialAccessContext plugin = CredentialAccessContext.forPlugin("apinity");

        assertTrue(policy.canView(credential, plugin));
        assertTrue(policy.canUse(credential, plugin));
    }

    @Test
    void whenPluginHasNoAccessGrantThenCannotViewOrUse() {
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS))
                .thenReturn(false);
        CredentialAccessContext plugin = CredentialAccessContext.forPlugin("apinity");

        assertFalse(policy.canView(credential, plugin));
        assertFalse(policy.canUse(credential, plugin));
    }

    @Test
    void whenLoadPluginViewAccessThenLoadOnlyAccessGrants() {
        CredentialAccessContext plugin = CredentialAccessContext.forPlugin("apinity");
        Set<CredentialAclPermission> allowedPermissions = Set.of(CredentialAclPermission.ACCESS);
        when(accessRepository.findAllByPrincipalTypeAndPrincipalIdAndPermissionIn(
                CredentialPrincipalType.PLUGIN, "apinity", allowedPermissions)).thenReturn(List.of());

        CredentialAccessPolicy.CredentialViewAccess viewAccess = policy.loadViewAccess(plugin);

        assertFalse(policy.canView(credential, plugin, viewAccess));
        verify(accessRepository).findAllByPrincipalTypeAndPrincipalIdAndPermissionIn(
                CredentialPrincipalType.PLUGIN, "apinity", allowedPermissions);
    }

    @Test
    void whenPluginActsForUserWithoutAccessGrantThenCannotViewOrUse() {
        CredentialAccessContext plugin = CredentialAccessContext.forUserAndPlugin(USER_ID, "apinity");
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS))
                .thenReturn(false);

        assertFalse(policy.canView(credential, plugin));
        assertFalse(policy.canUse(credential, plugin));
    }

    @Test
    void whenGuestRequestsManageThenCannotManageCredential() {
        CredentialAccessContext guest = CredentialAccessContext.forUser(UserDTO.GUEST_ID);

        assertFalse(policy.canManage(credential, guest));
    }

    @Test
    void whenGuestLoadsViewAccessThenLoadGuestGroupAccessGrants() {
        CredentialAccessEntity grant = new CredentialAccessEntity();
        grant.setCredential(credential);
        grant.setPrincipalType(CredentialPrincipalType.GROUP);
        grant.setPrincipalId(Integer.toString(org.pacos.core.component.security.domain.Role.GUEST_ROLE));
        grant.setPermission(CredentialAclPermission.ACCESS);
        Set<CredentialAclPermission> permissions =
                Set.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE);
        when(userRepository.findRoleIdsByUserId(UserDTO.GUEST_ID)).thenReturn(List.of(1));
        when(accessRepository.findAllByPrincipalTypeAndPrincipalIdInAndPermissionIn(
                CredentialPrincipalType.GROUP, List.of("1"), permissions)).thenReturn(List.of(grant));
        CredentialAccessContext guest = CredentialAccessContext.forUser(UserDTO.GUEST_ID);

        CredentialAccessPolicy.CredentialViewAccess viewAccess = policy.loadViewAccess(guest);

        assertTrue(policy.canView(credential, guest, viewAccess));
    }

    @Test
    void whenGuestActsThroughPluginThenPluginGrantIsRequiredInsteadOfGuestGroupGrant() {
        CredentialAccessContext guestPlugin = CredentialAccessContext.forUserAndPlugin(UserDTO.GUEST_ID, "apinity");
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS))
                .thenReturn(false);

        assertFalse(policy.canView(credential, guestPlugin));
        assertFalse(policy.canUse(credential, guestPlugin));
    }

    @Test
    void whenGuestOrPluginTriesToManageThenCannotManage() {
        assertFalse(policy.canManage(credential, CredentialAccessContext.forUser(UserDTO.GUEST_ID)));
        assertFalse(policy.canManage(credential, CredentialAccessContext.forPlugin("apinity")));
    }

    @Test
    void whenLoadViewAccessThenFetchGroupGrantsInOneQuery() {
        CredentialAccessEntity grant = new CredentialAccessEntity();
        grant.setCredential(credential);
        grant.setPrincipalType(CredentialPrincipalType.GROUP);
        grant.setPrincipalId("3");
        grant.setPermission(CredentialAclPermission.ACCESS);
        Set<CredentialAclPermission> permissions =
                Set.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE);
        when(userRepository.findRoleIdsByUserId(USER_ID)).thenReturn(List.of(3));
        when(accessRepository.findAllByPrincipalTypeAndPrincipalIdInAndPermissionIn(
                CredentialPrincipalType.GROUP, List.of("3"), permissions)).thenReturn(List.of(grant));

        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);
        CredentialAccessPolicy.CredentialViewAccess viewAccess = policy.loadViewAccess(user);

        assertTrue(policy.canView(credential, user, viewAccess));
        assertFalse(policy.canView(credential, CredentialAccessContext.forUser(USER_ID + 1), viewAccess));
        verify(accessRepository).findAllByPrincipalTypeAndPrincipalIdInAndPermissionIn(
                CredentialPrincipalType.GROUP, List.of("3"), permissions);
    }

    @Test
    void whenLoadedManageGrantThenSnapshotAllowsCredentialManagement() {
        CredentialAccessEntity grant = new CredentialAccessEntity();
        grant.setCredential(credential);
        grant.setPrincipalType(CredentialPrincipalType.GROUP);
        grant.setPrincipalId("3");
        grant.setPermission(CredentialAclPermission.MANAGE);
        Set<CredentialAclPermission> permissions =
                Set.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE);
        when(userRepository.findRoleIdsByUserId(USER_ID)).thenReturn(List.of(3));
        when(accessRepository.findAllByPrincipalTypeAndPrincipalIdInAndPermissionIn(
                CredentialPrincipalType.GROUP, List.of("3"), permissions)).thenReturn(List.of(grant));

        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);
        CredentialAccessPolicy.CredentialViewAccess viewAccess = policy.loadViewAccess(user);

        assertTrue(policy.canManage(credential, user, viewAccess));
    }

    @Test
    void whenLoadedAccessOnlyGrantThenSnapshotDoesNotAllowCredentialManagement() {
        CredentialAccessEntity grant = new CredentialAccessEntity();
        grant.setCredential(credential);
        grant.setPrincipalType(CredentialPrincipalType.GROUP);
        grant.setPrincipalId("3");
        grant.setPermission(CredentialAclPermission.ACCESS);
        Set<CredentialAclPermission> permissions =
                Set.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE);
        when(userRepository.findRoleIdsByUserId(USER_ID)).thenReturn(List.of(3));
        when(accessRepository.findAllByPrincipalTypeAndPrincipalIdInAndPermissionIn(
                CredentialPrincipalType.GROUP, List.of("3"), permissions)).thenReturn(List.of(grant));

        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);
        CredentialAccessPolicy.CredentialViewAccess viewAccess = policy.loadViewAccess(user);

        assertFalse(policy.canManage(credential, user, viewAccess));
    }

    @Test
    void whenGroupHasManageGrantThenCanManageCredentialAndAccess() {
        when(userRepository.findRoleIdsByUserId(USER_ID)).thenReturn(List.of(3));
        when(accessRepository.hasAccessForPrincipals(
                "credential-id", CredentialPrincipalType.GROUP, List.of("3"),
                Set.of(CredentialAclPermission.MANAGE)))
                .thenReturn(true);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);

        assertTrue(policy.canManage(credential, user));
    }
}

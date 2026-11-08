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
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.user.repository.UserRepository;
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
    private UserRepository userRepository;

    private CredentialAccessPolicy accessPolicy;
    private CredentialEntity credential;

    @BeforeEach
    void setUp() {
        accessPolicy = new CredentialAccessPolicy(accessRepository, userRepository);
        credential = new CredentialEntity();
        credential.setId("credential-id");
        credential.setOwnerUserId(20);
        credential.setVisibility(CredentialVisibility.PRIVATE);
    }

    @Test
    void whenGuestBelongsToGuestGroupWithAccessThenCanViewAndUsePrivateCredential() {
        when(userRepository.findRoleIdsByUserId(UserDTO.GUEST_ID)).thenReturn(List.of(1));
        when(accessRepository.hasAccessForPrincipals(
                "credential-id", CredentialPrincipalType.GROUP, List.of("1"),
                Set.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE)))
                .thenReturn(true);
        CredentialAccessContext guest = CredentialAccessContext.forUser(UserDTO.GUEST_ID);

        assertTrue(accessPolicy.canView(credential, guest));
        assertTrue(accessPolicy.canUse(credential, guest));
        assertFalse(accessPolicy.canManage(credential, guest));
    }

    @Test
    void whenGuestBelongsToGuestGroupWithoutAccessThenCannotViewOrUsePrivateCredential() {
        when(userRepository.findRoleIdsByUserId(UserDTO.GUEST_ID)).thenReturn(List.of(1));
        when(accessRepository.hasAccessForPrincipals(
                "credential-id", CredentialPrincipalType.GROUP, List.of("1"),
                Set.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE)))
                .thenReturn(false);
        CredentialAccessContext guest = CredentialAccessContext.forUser(UserDTO.GUEST_ID);

        assertFalse(accessPolicy.canView(credential, guest));
        assertFalse(accessPolicy.canUse(credential, guest));
    }

    @Test
    void whenPrivateCredentialAndUserHasNoGrantThenCannotViewOrUse() {
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);

        assertFalse(accessPolicy.canView(credential, user));
        assertFalse(accessPolicy.canUse(credential, user));
    }

    @Test
    void whenSharedCredentialThenCanViewAndUse() {
        credential.setVisibility(CredentialVisibility.SHARED);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);

        assertTrue(accessPolicy.canView(credential, user));
        assertTrue(accessPolicy.canUse(credential, user));
    }

    @Test
    void whenPluginHasAccessGrantThenCanViewAndUseCredential() {
        when(accessRepository.hasAccess(
                "credential-id", CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS))
                .thenReturn(true);
        CredentialAccessContext plugin = CredentialAccessContext.forPlugin("apinity");

        assertTrue(accessPolicy.canView(credential, plugin));
        assertTrue(accessPolicy.canUse(credential, plugin));
    }

    @Test
    void whenUserBelongsToGroupWithAccessThenCanViewAndUseCredential() {
        when(userRepository.findRoleIdsByUserId(USER_ID)).thenReturn(List.of(3));
        when(accessRepository.hasAccessForPrincipals(
                "credential-id", CredentialPrincipalType.GROUP, List.of("3"),
                Set.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE)))
                .thenReturn(true);
        CredentialAccessContext user = CredentialAccessContext.forUser(USER_ID);

        assertTrue(accessPolicy.canView(credential, user));
        assertTrue(accessPolicy.canUse(credential, user));
    }

    @Test
    void whenUserBelongsToGroupWithManageThenCanManageCredential() {
        when(userRepository.findRoleIdsByUserId(USER_ID)).thenReturn(List.of(3));
        when(accessRepository.hasAccessForPrincipals(
                "credential-id", CredentialPrincipalType.GROUP, List.of("3"),
                Set.of(CredentialAclPermission.MANAGE)))
                .thenReturn(true);

        assertTrue(accessPolicy.canManage(credential, CredentialAccessContext.forUser(USER_ID)));
    }
}

package org.pacos.core.component.credential.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredentialAccessGrantTest {

    @Test
    void whenGroupReceivesAccessThenCreateGrant() {
        assertDoesNotThrow(() -> new CredentialAccessGrant(
                CredentialPrincipalType.GROUP, "3", CredentialAclPermission.ACCESS));
    }

    @Test
    void whenInvalidGroupIdThenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new CredentialAccessGrant(
                CredentialPrincipalType.GROUP, "developers", CredentialAclPermission.ACCESS));
    }

    @Test
    void whenPluginReceivesAccessPermissionThenCreateGrant() {
        assertDoesNotThrow(() -> new CredentialAccessGrant(
                CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS));
    }

    @Test
    void whenPluginReceivesManagePermissionThenCreateGrant() {
        assertDoesNotThrow(() -> new CredentialAccessGrant(
                CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.MANAGE));
    }
}

package org.pacos.core.component.credential.service;

import java.io.Serializable;

/**
 * Metadata for one credential ACL grant.
 *
 * @param principalType type of principal receiving access
 * @param principalId stable group or plugin identifier
 * @param permission granted credential permission
 */
public record CredentialAccessDescriptor(
        CredentialPrincipalType principalType,
        String principalId,
        CredentialAclPermission permission) implements Serializable {
}

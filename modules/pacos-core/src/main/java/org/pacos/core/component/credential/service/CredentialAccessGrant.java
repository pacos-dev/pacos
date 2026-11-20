package org.pacos.core.component.credential.service;

import java.io.Serializable;
import java.util.Objects;

/**
 * Core-only request to assign one ACL permission to a group, plugin or application principal.
 *
 * <p>Principal-specific policy is enforced by the credential service: plugins, the PacOS application and
 * the GUEST group may receive {@code ACCESS}, but never {@code MANAGE}.</p>
 */
public record CredentialAccessGrant(
        CredentialPrincipalType principalType,
        String principalId,
        CredentialAclPermission permission) implements Serializable {

    public CredentialAccessGrant {
        Objects.requireNonNull(principalType, "Principal type is required");
        Objects.requireNonNull(permission, "Access permission is required");
        if (principalId == null || principalId.isBlank() || principalId.length() > 255) {
            throw new IllegalArgumentException("Principal id must contain between 1 and 255 characters");
        }
        if (principalType == CredentialPrincipalType.GROUP) {
            try {
                if (Integer.parseInt(principalId) < 1) {
                    throw new IllegalArgumentException("Group id must be positive");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Group principal id must be numeric", e);
            }
        }
    }
}

package org.pacos.base.credential;

import java.io.Serializable;
import java.util.Objects;

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
        if (principalType == CredentialPrincipalType.USER) {
            try {
                if (Integer.parseInt(principalId) <= 1) {
                    throw new IllegalArgumentException("User id must be positive");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("User principal id must be numeric", e);
            }
        }
    }
}

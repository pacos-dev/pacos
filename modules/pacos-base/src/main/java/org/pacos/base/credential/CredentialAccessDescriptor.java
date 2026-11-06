package org.pacos.base.credential;

import java.io.Serializable;

public record CredentialAccessDescriptor(
        CredentialPrincipalType principalType,
        String principalId,
        CredentialAclPermission permission) implements Serializable {
}

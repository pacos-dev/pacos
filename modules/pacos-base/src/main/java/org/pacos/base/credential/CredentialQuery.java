package org.pacos.base.credential;

import java.io.Serializable;

public record CredentialQuery(String type, String searchTerm) implements Serializable {

    public static CredentialQuery all() {
        return new CredentialQuery(null, null);
    }
}

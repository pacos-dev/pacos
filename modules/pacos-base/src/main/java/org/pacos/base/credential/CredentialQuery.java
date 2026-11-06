package org.pacos.base.credential;

import java.io.Serializable;

/**
 * Optional filters applied when discovering credentials.
 *
 * @param type stable credential type key, or {@code null} for all types
 * @param searchTerm text matched against credential name, type and description
 */
public record CredentialQuery(String type, String searchTerm) implements Serializable {

    public static CredentialQuery all() {
        return new CredentialQuery(null, null);
    }
}

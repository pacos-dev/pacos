package org.pacos.base.credential;

/**
 * Signals that a credential is missing or the current principal is not allowed to access it.
 *
 * <p>The service uses a shared message for these cases to avoid disclosing credential existence.</p>
 */
public class CredentialAccessException extends RuntimeException {

    public CredentialAccessException(String message) {
        super(message);
    }

    public CredentialAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

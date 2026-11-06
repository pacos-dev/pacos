package org.pacos.base.credential;

public class CredentialAccessException extends RuntimeException {

    public CredentialAccessException(String message) {
        super(message);
    }

    public CredentialAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

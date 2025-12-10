package org.pacos.base.security;

/**
 * Throw in case of missing permission
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String action) {
        super("Access denied for action: " + action);
    }
}

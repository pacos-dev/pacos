package org.pacos.core.component.credential.service;

/**
 * Permission levels supported by credential ACL entries.
 *
 * <p>{@link #MANAGE} also grants the ability to view and resolve the credential.</p>
 */
public enum CredentialAclPermission {
    ACCESS,
    MANAGE
}

package org.pacos.core.component.credential.service;

/**
 * Controls broad credential visibility independently of group and plugin ACL grants.
 *
 * <p>{@link #SHARED} makes a credential available to all user sessions. Plugin execution still
 * requires an explicit plugin {@code ACCESS} grant.</p>
 */
public enum CredentialVisibility {
    PRIVATE,
    SHARED
}

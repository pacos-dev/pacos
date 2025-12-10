package org.pacos.base.security;

/**
 * Security decision manager
 */
public interface SecurityManager {

    /**
     * @param permission permission key
     * Return true if user has the given permission.
     * If false display a notification about missing permission
     */
    boolean hasActionPermission(Permission permission);

    /**
     *
     * @param permission permission key
     * @return true if the user has the given permission
     */
    boolean hasPermission(Permission permission);

}

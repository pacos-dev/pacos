package org.pacos.base.security;

/**
 * Provides permission implementation
 */
public interface Permission {

    String getKey();

    String getLabel();

    String getCategory();

    String getDescription();
}

package org.pacos.base.security;

/**
 * Describe action that needs to be cover by security police
 *
 * @param key         e.g. "mockserver.start"
 * @param label       e.g. "Start Mock Server"
 * @param category    e.g. "Mock Server"
 */
public record ActionDescriptor(
        String key,
        String label,
        String category
) {

}
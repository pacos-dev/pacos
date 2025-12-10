package org.pacos.config.repository.metadata;

/**
 * Represent maven-metadata.xml
 */
public record Metadata(
        String groupId,
        String artifactId,
        Versioning versioning) {
}
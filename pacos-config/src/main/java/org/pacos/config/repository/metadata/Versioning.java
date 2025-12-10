package org.pacos.config.repository.metadata;

public record Versioning(
        String latest,
        String release,
        Versions versions,
        String lastUpdated) {
}
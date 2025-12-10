package org.pacos.config.repository.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents module.json configuration fetching from remote server
 *
 * @param version      - current configuration version (system version)
 * @param modules - list of mandatory artifacts
 */
public record ModuleConfiguration(@JsonProperty("version") String version, @JsonProperty("modules") List<AppArtifact> modules) {
}

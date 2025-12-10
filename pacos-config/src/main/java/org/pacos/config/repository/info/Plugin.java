package org.pacos.config.repository.info;

import org.pacos.config.repository.data.AppArtifact;

/**
 * Comes from plugin.json
 */
public record Plugin(
        String groupId,
        String artifactName,
        String name,
        String version,
        String author,
        String icon) {

    public AppArtifact toAppArtifact() {
        return new AppArtifact(groupId, artifactName, version);
    }

    @Override
    public String toString() {
        return "Plugin{" +
                "groupId='" + groupId + '\'' +
                ", artifactName='" + artifactName + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", author='" + author + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}

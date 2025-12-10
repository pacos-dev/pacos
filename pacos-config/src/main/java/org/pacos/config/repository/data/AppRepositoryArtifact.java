package org.pacos.config.repository.data;

public record AppRepositoryArtifact(AppArtifact artifact, AppRepository repository) {
    public String getMetaDataUrl() {
        String groupPart = artifact.groupId().replace(".", "/");
        return buildUrlPath(repository.url(), groupPart, artifact.artifactName(), "maven-metadata.xml");
    }

    public String getFileUrl(String extension) {
        String groupPart = artifact.groupId().replace(".", "/");
        return buildUrlPath(repository.url(), groupPart, artifact.artifactName(), artifact.version(), artifact.getFileName(extension));
    }

    private String buildUrlPath(String... args) {
        StringBuilder builder = new StringBuilder();
        for (String val : args) {
            builder.append(val);
            builder.append("/");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    @Override
    public String toString() {
        return "AppRepositoryArtifact{" +
                "artifact=" + artifact +
                ", repository=" + repository +
                '}';
    }
}

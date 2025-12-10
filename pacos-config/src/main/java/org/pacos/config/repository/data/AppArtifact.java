package org.pacos.config.repository.data;

import java.io.File;

public record AppArtifact(

        String groupId,

        String artifactName,

        String version) {

    public String getJarFileName() {
        return getFileName("jar");
    }

    public String getPomFileName() {
        return getFileName("pom");
    }

    public String getFileName(String extension) {
        return artifactName + "-" + version + "." + extension;
    }

    public String getJarPath() {
        String groupPart = groupId.replace(".", File.separator);
        return buildPath(groupPart, artifactName, version, getJarFileName());
    }

    public String getDirPath() {
        String groupPart = groupId.replace(".", File.separator);
        return buildPath(groupPart, artifactName, version);
    }

    private String buildPath(String... args) {
        StringBuilder builder = new StringBuilder();
        for (String val : args) {
            builder.append(val);
            builder.append(File.separator);
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public AppArtifact forVersion(String newVersion){
        return new AppArtifact(groupId,artifactName,newVersion);
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "groupId='" + groupId + '\'' +
                ", artifactName='" + artifactName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

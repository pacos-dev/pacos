package org.pacos.config.repository.data;

import java.io.File;
import java.util.regex.Pattern;

public record AppArtifact(

        String groupId,

        String artifactName,

        String version) {

    private static final Pattern GROUP_ID = Pattern.compile("[A-Za-z0-9_-]++(?:\\.[A-Za-z0-9_-]++)*+");
    private static final Pattern ARTIFACT_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]*");
    private static final Pattern VERSION = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._+-]*+");
    private static final Pattern EXTENSION = Pattern.compile("[A-Za-z0-9][A-Za-z0-9.-]*");

    public String getJarFileName() {
        return getFileName("jar");
    }

    public String getPomFileName() {
        return getFileName("pom");
    }

    public String getFileName(String extension) {
        validateCoordinates();
        if (extension == null || !EXTENSION.matcher(extension).matches()) {
            throw new IllegalArgumentException("Invalid artifact file extension");
        }
        return artifactName + "-" + version + "." + extension;
    }

    public String getJarPath() {
        validateCoordinates();
        String groupPart = groupId.replace(".", File.separator);
        return buildPath(groupPart, artifactName, version, getJarFileName());
    }

    public String getDirPath() {
        validateCoordinates();
        String groupPart = groupId.replace(".", File.separator);
        return buildPath(groupPart, artifactName, version);
    }

    private void validateCoordinates() {
        if (groupId == null || !GROUP_ID.matcher(groupId).matches()
                || artifactName == null || !ARTIFACT_NAME.matcher(artifactName).matches()
                || version == null || !VERSION.matcher(version).matches()) {
            throw new IllegalArgumentException("Invalid Maven artifact coordinates; unsafe path components are not allowed");
        }
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

    public AppArtifact forVersion(String newVersion) {
        return new AppArtifact(groupId, artifactName, newVersion);
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

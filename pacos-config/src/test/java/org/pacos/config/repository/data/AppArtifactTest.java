package org.pacos.config.repository.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;

class AppArtifactTest {

    @Test
    void whenGetJarFileNameThenReturnPathWithExtension() {
        AppArtifact artifact = new AppArtifact("group", "name", "version");

        assertEquals("name-version.jar", artifact.getJarFileName());
    }

    @Test
    void whenGetPomFileNameThenReturnPathWithExtension() {
        AppArtifact artifact = new AppArtifact("group", "name", "version");

        assertEquals("name-version.pom", artifact.getPomFileName());
    }

    @Test
    void whenGetDirPathThenReturnPathWithoutFileName() {
        AppArtifact artifact = new AppArtifact("group", "name", "version");
        String dirPath = "group" + File.separator + "name" + File.separator + "version";

        assertEquals(dirPath, artifact.getDirPath());
    }

    @Test
    void whenGetFilePathThenReturnJarLocalisation() {
        AppArtifact artifact = new AppArtifact("group", "name", "version");
        String dirPath = "group" + File.separator + "name" + File.separator + "version";

        assertEquals(dirPath + File.separator + artifact.getJarFileName(), artifact.getJarPath());
    }

    @Test
    void whenGetJarPathThenRejectPathSeparatorsInGroupId() {
        AppArtifact artifact = new AppArtifact("org.pacos/../../outside", "name", "1.0");

        assertThrows(IllegalArgumentException.class, artifact::getJarPath);
    }

    @Test
    void whenGetJarPathThenRejectPathTraversalInArtifactName() {
        AppArtifact artifact = new AppArtifact("org.pacos", "../outside", "1.0");

        assertThrows(IllegalArgumentException.class, artifact::getJarPath);
    }

    @Test
    void whenGetJarPathThenRejectPathTraversalInVersion() {
        AppArtifact artifact = new AppArtifact("org.pacos", "name", "../outside");

        assertThrows(IllegalArgumentException.class, artifact::getJarPath);
    }

    @Test
    void whenGetFileNameThenRejectPathSeparatorInExtension() {
        AppArtifact artifact = new AppArtifact("org.pacos", "name", "1.0");

        assertThrows(IllegalArgumentException.class, () -> artifact.getFileName("../outside"));
        assertThrows(IllegalArgumentException.class, () -> artifact.getFileName("..\\outside"));
    }

    @Test
    void whenGetFileNameThenRejectNullOrEmptyExtension() {
        AppArtifact artifact = new AppArtifact("org.pacos", "name", "1.0");

        assertThrows(IllegalArgumentException.class, () -> artifact.getFileName(null));
        assertThrows(IllegalArgumentException.class, () -> artifact.getFileName(""));
    }

    @Test
    void whenGetFileNameThenRejectInvalidArtifactCoordinates() {
        AppArtifact nullGroupId = new AppArtifact(null, "name", "1.0");
        AppArtifact nullArtifactName = new AppArtifact("org.pacos", null, "1.0");
        AppArtifact nullVersion = new AppArtifact("org.pacos", "name", null);
        AppArtifact emptyGroupId = new AppArtifact("", "name", "1.0");
        AppArtifact emptyVersion = new AppArtifact("org.pacos", "name", "");

        assertThrows(IllegalArgumentException.class, () -> nullGroupId.getFileName("jar"));
        assertThrows(IllegalArgumentException.class, () -> nullArtifactName.getFileName("jar"));
        assertThrows(IllegalArgumentException.class, () -> nullVersion.getFileName("jar"));
        assertThrows(IllegalArgumentException.class, () -> emptyGroupId.getFileName("jar"));
        assertThrows(IllegalArgumentException.class, () -> emptyVersion.getFileName("jar"));
    }

    @Test
    void whenToStringThenReturnFormattedValue() {
        AppArtifact artifact = new AppArtifact("group", "name", "version");

        assertEquals("Artifact{groupId='group', artifactName='name', version='version'}", artifact.toString());
    }
}

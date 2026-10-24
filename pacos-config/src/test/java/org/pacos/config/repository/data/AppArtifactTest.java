package org.pacos.config.repository.data;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppArtifactTest {

    @Test
    void whenGetJarFileNameThenReturnPathWithExtension() {
        //given
        AppArtifact artifact = new AppArtifact("group", "name", "version");
        //then
        assertEquals("name-version.jar", artifact.getJarFileName());
    }

    @Test
    void whenGetPomFileNameThenReturnPathWithExtension() {
        //given
        AppArtifact artifact = new AppArtifact("group", "name", "version");
        //then
        assertEquals("name-version.pom", artifact.getPomFileName());
    }


    @Test
    void whenGetDirPathThenReturnPathWithoutFileName() {
        //given
        AppArtifact artifact = new AppArtifact("group", "name", "version");
        //then
        String dirPath = "group" + File.separator + "name" + File.separator + "version";
        assertEquals(dirPath, artifact.getDirPath());
    }


    @Test
    void whenGetFilePathThenReturnJarLocalisation() {
        //given
        AppArtifact artifact = new AppArtifact("group", "name", "version");
        //then
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
    }

    @Test
    void whenToStringThenReturnFormattedValue() {
        AppArtifact artifact = new AppArtifact("group", "name", "version");
        //then
        assertEquals("Artifact{groupId='group', artifactName='name', version='version'}", artifact.toString());
    }


}
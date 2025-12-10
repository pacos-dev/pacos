package org.pacos.core.component.plugin.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.config.property.PropertyName;
import org.pacos.config.property.WorkingDir;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginIconExtractorTest {

    @TempDir
    private Path tempDir;

    @Test
    void whenExtractIconThenStoreInPluginDir() throws IOException {
        System.setProperty("-D" + PropertyName.WORKING_DIR.getPropertyName(), tempDir.toString());
        WorkingDir.initialize();
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.0.0");
        pluginDTO.setGroupId("org.pacos");
        WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getDirPath()).toFile().mkdirs();
        File pluginJar = new File(getClass().getResource("/plugin/test-jar-with-icon.jar").getFile());

        Files.copy(pluginJar.toPath(), WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getJarPath()));
        PluginIconExtractor.extractIcon(pluginDTO);
        //then
        Path icon = WorkingDir.getModulePath("test").resolve("icon").resolve(pluginDTO.getArtifactName() + ".png");
        assertTrue(icon.toFile().exists());
    }

    @Test
    void whenExtractIconThenIconNotFound() throws IOException {
        System.setProperty("-D" + PropertyName.WORKING_DIR.getPropertyName(), tempDir.toString());
        WorkingDir.initialize();
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.0.0");
        pluginDTO.setGroupId("org.pacos");
        WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getDirPath()).toFile().mkdirs();
        File pluginJar = new File(getClass().getResource("/plugin/test-jar-with-missing-manifest.jar").getFile());

        Files.copy(pluginJar.toPath(), WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getJarPath()));
        PluginIconExtractor.extractIcon(pluginDTO);
        //then
        Path icon = WorkingDir.getModulePath("test").resolve("icon").resolve(pluginDTO.getArtifactName() + ".png");
        assertFalse(icon.toFile().exists());
    }
}
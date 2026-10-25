package org.pacos.core.component.plugin.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.config.property.PropertyName;
import org.pacos.config.property.WorkingDir;
import org.pacos.core.component.plugin.dto.PluginDTO;

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
        Path pluginJar = WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getJarPath());
        copyResourceTo("/plugin/test-jar-with-icon.jar", pluginJar);
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
        Path pluginJar = WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getJarPath());
        copyResourceTo("/plugin/test-jar-with-missing-manifest.jar", pluginJar);
        PluginIconExtractor.extractIcon(pluginDTO);
        //then
        Path icon = WorkingDir.getModulePath("test").resolve("icon").resolve(pluginDTO.getArtifactName() + ".png");
        assertFalse(icon.toFile().exists());
    }

    private void copyResourceTo(String resourceName, Path destination) throws IOException {
        try (InputStream resource = Objects.requireNonNull(
                getClass().getResourceAsStream(resourceName), "Missing test resource: " + resourceName)) {
            Files.copy(resource, destination);
        }
    }
}
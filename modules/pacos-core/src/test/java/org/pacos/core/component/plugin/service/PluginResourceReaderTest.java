package org.pacos.core.component.plugin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.pacos.config.property.PropertyName;
import org.pacos.config.property.WorkingDir;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.data.PluginJar;

@Isolated("Shares JVM-wide system properties or other global test resources; keep isolated during parallel runs.")
class PluginResourceReaderTest {

    @TempDir
    private Path tempDir;

    @Test
    void whenReadResourcesThenReturnListOfFileName() throws IOException {
        System.setProperty("-D" + PropertyName.WORKING_DIR.getPropertyName(), tempDir.toString());
        WorkingDir.initialize();
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.0.0");
        pluginDTO.setGroupId("org.pacos");
        WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getDirPath()).toFile().mkdirs();
        try (InputStream pluginJar = Objects.requireNonNull(
                getClass().getResourceAsStream("/plugin/test-jar-with-resources.jar"),
                "Missing test resource: /plugin/test-jar-with-resources.jar")) {
            Files.copy(pluginJar, WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getJarPath()));
        }
        Set<String> resources = PluginResourceReader.readResourceList(new PluginJar(pluginDTO));
        //then
        assertEquals(2, resources.size());
        assertTrue(resources.contains("/frontend/css/todo.css"));
        assertTrue(resources.contains("/img/icon/to-do-list.png"));
    }

}
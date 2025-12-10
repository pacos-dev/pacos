package org.pacos.core.component.plugin.service;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.PomDependencyResolver;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class PluginDownloadServiceTest {

    @TempDir
    private Path path;
    private AppRepository repository;
    private AppArtifact artifact;
    private Plugin plugin;

    @BeforeEach
    void init() {
        System.setProperty("workingDir", path.toString());
        WorkingDir.initialize();
        this.artifact = new AppArtifact("org.pacos", "test", "1.0");
        this.plugin = new Plugin("org.pacos", "test", "test", "1.0", "", "");
        this.repository = new AppRepository("id", "url");
    }

    @Test
    void whenDownloadPluginCalledThenResolveJarFails() {

        PluginDTO pluginDTO = new PluginDTO(plugin);

        try (var mockedResolver = mockStatic(PomDependencyResolver.class)) {
            mockedResolver.when(() -> PomDependencyResolver.resolve(artifact, repository, "jar")).thenReturn(false);

            PluginDTO result = PluginDownloadService.downloadPlugin(repository, artifact, pluginDTO);

            assertEquals("Can't resolve jar for plugin " + artifact, result.getErrMsg());
            mockedResolver.verify(() -> PomDependencyResolver.resolve(artifact, repository, "jar"), times(1));
        }
    }

    @Test
    void whenAnyExceptionDuringResolvingJarFileThenSetErrorMsg() {
        PluginDTO pluginDTO = new PluginDTO(plugin);

        try (var mockedResolver = mockStatic(PomDependencyResolver.class)) {
            mockedResolver.when(() -> PomDependencyResolver.resolve(artifact, repository, "jar")).thenReturn(false);

            PluginDTO resolved = PluginDownloadService.downloadPlugin(repository, artifact, pluginDTO);
            assertEquals("Can't resolve jar for plugin Artifact{groupId='org.pacos', artifactName='test', version='1.0'}", resolved.getErrMsg());
        }
    }

}

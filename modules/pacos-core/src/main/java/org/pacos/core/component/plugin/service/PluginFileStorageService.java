package org.pacos.core.component.plugin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.pacos.base.exception.PacosException;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
import org.springframework.stereotype.Service;

@Service
public class PluginFileStorageService {
    private final PacosPluginRepository pluginRepository;

    public PluginFileStorageService(PacosPluginRepository pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    public void storePluginFile(UploadedPluginInfo pluginInfo) throws IOException {
        AppArtifact artifact = pluginInfo.pluginDTO().toArtifact();
        if (!pluginRepository.findByArtifactNameAndGroupId(artifact.artifactName(), artifact.groupId()).isEmpty()) {
            throw new PacosException("This plugin is already installed. Remove existing installed plugin first.");
        }
        Path destinationDir = WorkingDir.getLibPath().resolve(artifact.getDirPath());
        Path destinationFile = destinationDir.resolve(artifact.getJarFileName());

        if (Files.exists(destinationFile) && !Files.deleteIfExists(destinationFile)) {
            throw new PacosException("You cannot overwrite a plugin you are using with the same version. " +
                    "Disable the plugin or remove it if you want to reinstall it. You can also change its version.");
        }

        Files.createDirectories(destinationDir);
        Files.write(destinationFile, pluginInfo.fileData());
    }
}

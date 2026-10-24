package org.pacos.core.component.plugin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.pacos.base.exception.PacosException;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;
import org.springframework.stereotype.Service;

@Service
public class PluginFileStorageService {
    private static final Pattern GROUP_ID = Pattern.compile("[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*");
    private static final Pattern ARTIFACT_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]*");
    private static final Pattern VERSION = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._+-]*");

    private final PacosPluginRepository pluginRepository;

    public PluginFileStorageService(PacosPluginRepository pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    public void storePluginFile(UploadedPluginInfo pluginInfo) throws IOException {
        AppArtifact artifact = pluginInfo.pluginDTO().toArtifact();
        validateCoordinates(artifact);

        if (!pluginRepository.findByArtifactNameAndGroupId(artifact.artifactName(), artifact.groupId()).isEmpty()) {
            throw new PacosException("This plugin is already installed. Remove existing installed plugin first.");
        }

        Path libRoot = WorkingDir.getLibPath().toAbsolutePath().normalize();
        Path destinationDir = libRoot.resolve(artifact.getDirPath()).normalize();
        Path destinationFile = destinationDir.resolve(artifact.getJarFileName()).normalize();
        if (!destinationDir.startsWith(libRoot) || !destinationFile.startsWith(destinationDir)) {
            throw new PacosException("Invalid plugin coordinates: storage path escapes the plugin directory.");
        }

        Files.createDirectories(destinationDir);

        // Resolve symlinks before writing so a pre-existing link cannot redirect the upload outside the library.
        Path realLibRoot = libRoot.toRealPath();
        Path realDestinationDir = destinationDir.toRealPath();
        if (!realDestinationDir.startsWith(realLibRoot)) {
            throw new PacosException("Invalid plugin coordinates: storage path escapes the plugin directory.");
        }
        if (Files.isSymbolicLink(destinationFile)) {
            throw new PacosException("Refusing to overwrite a symbolic link in the plugin directory.");
        }

        if (Files.exists(destinationFile) && !Files.deleteIfExists(destinationFile)) {
            throw new PacosException("You cannot overwrite a plugin you are using with the same version. " +
                    "Disable the plugin or remove it if you want to reinstall it. You can also change its version.");
        }

        Files.write(destinationFile, pluginInfo.fileData());
    }

    private static void validateCoordinates(AppArtifact artifact) {
        if (artifact.groupId() == null || !GROUP_ID.matcher(artifact.groupId()).matches()
                || artifact.artifactName() == null || !ARTIFACT_NAME.matcher(artifact.artifactName()).matches()
                || artifact.version() == null || !VERSION.matcher(artifact.version()).matches()) {
            throw new PacosException("Invalid plugin coordinates. Group ID, artifact name, and version must be valid Maven coordinates.");
        }
    }
}

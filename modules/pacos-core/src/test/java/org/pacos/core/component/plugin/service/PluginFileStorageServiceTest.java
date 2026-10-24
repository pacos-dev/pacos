package org.pacos.core.component.plugin.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.pacos.base.exception.PacosException;
import org.pacos.config.property.WorkingDir;
import org.pacos.core.component.plugin.domain.AppPlugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;

class PluginFileStorageServiceTest {

    private final PacosPluginRepository pluginRepository = mock(PacosPluginRepository.class);
    private final PluginFileStorageService service = new PluginFileStorageService(pluginRepository);

    @TempDir
    private Path temporaryDirectory;

    @Test
    void whenStorePluginFileThenCreateDirectoriesAndWriteContent() throws IOException {
        Path libRoot = Files.createDirectory(temporaryDirectory.resolve("lib"));
        UploadedPluginInfo info = pluginInfo("org.pacos", "test-plugin", "1.0-SNAPSHOT");

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(libRoot);

            service.storePluginFile(info);
        }

        Path destination = libRoot.resolve("org/pacos/test-plugin/1.0-SNAPSHOT/test-plugin-1.0-SNAPSHOT.jar");
        assertArrayEquals(info.fileData(), Files.readAllBytes(destination));
        assertFalse(Files.exists(libRoot.resolve(info.fileName())));
        verify(pluginRepository).findByArtifactNameAndGroupId("test-plugin", "org.pacos");
    }

    @Test
    void whenStorePluginFileWithExistingFileThenReplaceContent() throws IOException {
        Path libRoot = Files.createDirectory(temporaryDirectory.resolve("lib"));
        UploadedPluginInfo info = pluginInfo("org.pacos", "test-plugin", "1.0");
        Path destination = libRoot.resolve("org/pacos/test-plugin/1.0/test-plugin-1.0.jar");
        Files.createDirectories(destination.getParent());
        Files.write(destination, new byte[] { 9, 8, 7, 6 });

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(libRoot);

            service.storePluginFile(info);
        }

        assertArrayEquals(info.fileData(), Files.readAllBytes(destination));
    }

    @Test
    void whenStorePluginFileWithInstalledPluginThenRejectBeforeAccessingWorkingDirectory() {
        UploadedPluginInfo info = pluginInfo("org.pacos", "test-plugin", "1.0");
        when(pluginRepository.findByArtifactNameAndGroupId("test-plugin", "org.pacos"))
                .thenReturn(List.of(mock(AppPlugin.class)));

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            PacosException exception = assertThrows(PacosException.class, () -> service.storePluginFile(info));

            assertEquals("This plugin is already installed. Remove existing installed plugin first.",
                    exception.getMessage());
            workingDir.verifyNoInteractions();
        }
    }

    @ParameterizedTest
    @MethodSource("invalidCoordinates")
    void whenStorePluginFileWithInvalidCoordinatesThenRejectBeforeRepositoryLookup(
            String groupId, String artifactName, String version) {
        UploadedPluginInfo info = pluginInfo(groupId, artifactName, version);

        PacosException exception = assertThrows(PacosException.class, () -> service.storePluginFile(info));

        assertEquals("Invalid plugin coordinates. Group ID, artifact name, and version must be valid Maven coordinates.",
                exception.getMessage());
        verifyNoInteractions(pluginRepository);
    }

    @ParameterizedTest
    @MethodSource("longValidCoordinates")
    void whenStorePluginFileWithLongValidCoordinatesThenReachRepositoryLookupWithoutStackOverflow(
            String groupId, String artifactName, String version) {
        UploadedPluginInfo info = pluginInfo(groupId, artifactName, version);
        when(pluginRepository.findByArtifactNameAndGroupId(artifactName, groupId))
                .thenReturn(List.of(mock(AppPlugin.class)));

        PacosException exception = assertThrows(PacosException.class, () -> service.storePluginFile(info));

        assertEquals("This plugin is already installed. Remove existing installed plugin first.",
                exception.getMessage());
        verify(pluginRepository).findByArtifactNameAndGroupId(artifactName, groupId);
    }

    private static Stream<Arguments> invalidCoordinates() {
        Stream<Arguments> invalidGroups = Stream.of(null, "", ".org", "org.", "org..pacos", "../outside",
                        "org/pacos", "org\\pacos", "org pacos", "org.pacos!", "a.".repeat(20_000) + "!")
                .map(groupId -> Arguments.of(groupId, "test-plugin", "1.0"));
        Stream<Arguments> invalidNames = Stream.of(null, "", ".", "..", "../outside", "test/plugin",
                        "test\\plugin", "-test", "test plugin", "a".repeat(100_000) + "!")
                .map(artifactName -> Arguments.of("org.pacos", artifactName, "1.0"));
        Stream<Arguments> invalidVersions = Stream.of(null, "", ".", "..", "../outside", "1/0",
                        "1\\0", "-1", "1 0", "1".repeat(100_000) + "!")
                .map(version -> Arguments.of("org.pacos", "test-plugin", version));
        return Stream.concat(Stream.concat(invalidGroups, invalidNames), invalidVersions);
    }

    private static Stream<Arguments> longValidCoordinates() {
        return Stream.of(
                Arguments.of("a.".repeat(20_000) + "a", "test-plugin", "1.0"),
                Arguments.of("a".repeat(100_000), "test-plugin", "1.0"),
                Arguments.of("org.pacos", "a".repeat(100_000), "1.0"),
                Arguments.of("org.pacos", "test-plugin", "1".repeat(100_000)));
    }

    private static UploadedPluginInfo pluginInfo(String groupId, String artifactName, String version) {
        PluginDTO plugin = new PluginDTO();
        plugin.setGroupId(groupId);
        plugin.setArtifactName(artifactName);
        plugin.setVersion(version);
        return new UploadedPluginInfo(plugin, new byte[] { 1, 2, 3 }, "uploaded.jar");
    }
}

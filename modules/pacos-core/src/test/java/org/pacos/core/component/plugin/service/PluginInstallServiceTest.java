package org.pacos.core.component.plugin.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pacos.base.exception.PacosException;
import org.pacos.config.property.WorkingDir;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;

class PluginInstallServiceTest {

    @InjectMocks
    private PluginFileStorageService pluginFileStorageService;
    @Mock
    private PacosPluginRepository pluginRepository;
    @TempDir
    private Path tmpDir;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenStorePluginUploadedByUserThenPutInCorrectLocation() throws IOException {
        Path fileToUpload = createTestFile();
        PluginDTO pluginDTO = createTestPlugin();

        try (FileInputStream inputStream = new FileInputStream(fileToUpload.toFile());
             MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(tmpDir.resolve("lib"));
            UploadedPluginInfo info = new UploadedPluginInfo(pluginDTO, inputStream.readAllBytes(), "artifact-1.0.jar");
            when(pluginRepository.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId()))
                    .thenReturn(List.of());

            pluginFileStorageService.storePluginFile(info);
        }

        Path storedFile = tmpDir.resolve("lib").resolve(pluginDTO.toArtifact().getJarPath());
        assertTrue(Files.isRegularFile(storedFile));
        assertArrayEquals(Files.readAllBytes(fileToUpload), Files.readAllBytes(storedFile));
    }

    @Test
    void whenDestinationPathContainsRegularFileInsteadOfDirectoryThenRejectUpload() throws IOException {
        Path libRoot = tmpDir.resolve("lib");
        Path groupDirectory = libRoot.resolve("pl");
        Files.createDirectories(libRoot);
        Files.write(groupDirectory, new byte[]{7});
        PluginDTO pluginDTO = createTestPlugin();

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(libRoot);
            when(pluginRepository.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId()))
                    .thenReturn(List.of());

            assertThrows(PacosException.class,
                    () -> uploadPlugin(pluginDTO, new byte[] { 1 }, "artifact-1.0.jar"));
        }

        assertTrue(Files.isRegularFile(groupDirectory));
        assertArrayEquals(new byte[]{7}, Files.readAllBytes(groupDirectory));
    }

    @Test
    void whenLibraryRootDoesNotExistThenRejectUpload() {
        Path libRoot = tmpDir.resolve("missing-lib");
        PluginDTO pluginDTO = createTestPlugin();

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(libRoot);
            when(pluginRepository.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId()))
                    .thenReturn(List.of());

            assertThrows(PacosException.class,
                    () -> uploadPlugin(pluginDTO, new byte[] { 1 }, "test.jar"));
        }

        assertFalse(Files.exists(libRoot));
    }

    @Test
    void whenPluginDirectoryContainsSymlinkThenRejectUploadWithoutCreatingOutsideDirectories() throws IOException {
        Path libRoot = tmpDir.resolve("lib");
        Path outside = tmpDir.resolve("outside");
        Files.createDirectories(libRoot);
        Files.createDirectories(outside);
        Files.createSymbolicLink(libRoot.resolve("pl"), outside);
        PluginDTO pluginDTO = createTestPlugin();

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(libRoot);
            when(pluginRepository.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId()))
                    .thenReturn(List.of());

            assertThrows(PacosException.class,
                    () -> uploadPlugin(pluginDTO, new byte[] { 1 }, "test.jar"));
        }

        assertFalse(Files.exists(outside.resolve("pacos/test/1.1")));
    }

    @Test
    void whenDestinationFileIsSymlinkThenRejectUploadWithoutChangingTarget() throws IOException {
        Path libRoot = tmpDir.resolve("lib");
        Path outsideFile = tmpDir.resolve("outside.jar");
        PluginDTO pluginDTO = createTestPlugin();
        byte[] originalContent = new byte[]{9, 8, 7};
        Files.createDirectories(libRoot.resolve("pl/pacos/test/1.1"));
        Files.write(outsideFile, originalContent);
        Path destinationFile = libRoot.resolve(pluginDTO.toArtifact().getJarPath());
        Files.createSymbolicLink(destinationFile, outsideFile);

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(libRoot);
            when(pluginRepository.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId()))
                    .thenReturn(List.of());

            assertThrows(PacosException.class,
                    () -> uploadPlugin(pluginDTO, new byte[] { 1 }, "test.jar"));
        }

        assertTrue(Files.isSymbolicLink(destinationFile));
        assertArrayEquals(originalContent, Files.readAllBytes(outsideFile));
    }

    @Test
    void whenUploadedPluginArtifactNameContainsPathTraversalThenRejectUploadBeforeRepositoryLookup() {
        PluginDTO pluginDTO = createTestPlugin();
        pluginDTO.setArtifactName("../outside");

        assertThrows(PacosException.class,
                () -> uploadPlugin(pluginDTO, new byte[] { 1 }, "outside.jar"));
        verifyNoInteractions(pluginRepository);
    }

    @Test
    void whenUploadedPluginGroupIdContainsPathSeparatorsThenRejectUploadBeforeRepositoryLookup() {
        PluginDTO pluginDTO = createTestPlugin();
        pluginDTO.setGroupId("pl.pacos/../../outside");

        assertThrows(PacosException.class,
                () -> uploadPlugin(pluginDTO, new byte[] { 1 }, "outside.jar"));
        verifyNoInteractions(pluginRepository);
    }

    @Test
    void whenUploadedPluginVersionContainsPathTraversalThenRejectUploadBeforeRepositoryLookup() {
        PluginDTO pluginDTO = createTestPlugin();
        pluginDTO.setVersion("../outside");

        assertThrows(PacosException.class,
                () -> uploadPlugin(pluginDTO, new byte[] { 1 }, "outside.jar"));
        verifyNoInteractions(pluginRepository);
    }

    private void uploadPlugin(PluginDTO pluginDTO, byte[] contents, String fileName) throws IOException {
        pluginFileStorageService.storePluginFile(new UploadedPluginInfo(pluginDTO, contents, fileName));
    }

    private static PluginDTO createTestPlugin() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setGroupId("pl.pacos");
        pluginDTO.setArtifactName("test");
        pluginDTO.setVersion("1.1");
        pluginDTO.setName("Awesome test");
        return pluginDTO;
    }

    private Path createTestFile() throws IOException {
        Path fileToUpload = tmpDir.resolve("artifact-1.0.jar");
        Files.createDirectories(tmpDir.resolve("lib"));
        createJarWithManifest(fileToUpload);
        return fileToUpload;
    }

    private static void createJarWithManifest(Path jarPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(jarPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.putNextEntry(new ZipEntry("META-INF/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            zos.write("test".getBytes());
            zos.closeEntry();
        }
    }
}

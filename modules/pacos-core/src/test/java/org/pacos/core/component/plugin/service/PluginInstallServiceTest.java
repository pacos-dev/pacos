package org.pacos.core.component.plugin.service;

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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
            when(pluginRepository.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId())).thenReturn(List.of());
            //when
            pluginFileStorageService.storePluginFile(info);
        }
        assertTrue(tmpDir.resolve("lib").resolve(pluginDTO.toArtifact().getJarPath()).toFile().exists());
    }


    @Test
    void whenFileExistsAndCantBeDeletedThenDoNotThrowException() throws IOException {
        Path fileToUpload = createTestFile();
        PluginDTO pluginDTO = createTestPlugin();
        assertTrue(tmpDir.resolve("lib").resolve(pluginDTO.toArtifact().getJarPath()).toFile().mkdirs());

        try (FileInputStream inputStream = new FileInputStream(fileToUpload.toFile());
             MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            filesMock.when(() -> Files.deleteIfExists(any())).thenReturn(false);
            workingDir.when(WorkingDir::getLibPath).thenReturn(tmpDir.resolve("lib"));
            when(pluginRepository.findByArtifactNameAndGroupId(pluginDTO.getArtifactName(), pluginDTO.getGroupId())).thenReturn(List.of());
            UploadedPluginInfo info = new UploadedPluginInfo(pluginDTO, inputStream.readAllBytes(), "artifact-1.0.jar");
            //when
            assertDoesNotThrow(()->pluginFileStorageService.storePluginFile(info));
        }
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
                    () -> pluginFileStorageService.storePluginFile(
                            new UploadedPluginInfo(pluginDTO, new byte[]{1}, "test.jar")));
        }

        assertFalse(Files.exists(outside.resolve("pacos/test/1.1")));
    }

    @Test
    void whenUploadedPluginArtifactNameContainsPathTraversalThenRejectUpload() {
        PluginDTO pluginDTO = createTestPlugin();
        pluginDTO.setArtifactName("../outside");

        assertThrows(PacosException.class,
                () -> pluginFileStorageService.storePluginFile(
                        new UploadedPluginInfo(pluginDTO, new byte[]{1}, "outside.jar")));
    }

    @Test
    void whenUploadedPluginGroupIdContainsPathSeparatorsThenRejectUpload() {
        PluginDTO pluginDTO = createTestPlugin();
        pluginDTO.setGroupId("pl.pacos/../../outside");

        assertThrows(PacosException.class,
                () -> pluginFileStorageService.storePluginFile(
                        new UploadedPluginInfo(pluginDTO, new byte[]{1}, "outside.jar")));
    }

    @Test
    void whenUploadedPluginVersionContainsPathTraversalThenRejectUpload() {
        PluginDTO pluginDTO = createTestPlugin();
        pluginDTO.setVersion("../outside");

        assertThrows(PacosException.class,
                () -> pluginFileStorageService.storePluginFile(
                        new UploadedPluginInfo(pluginDTO, new byte[]{1}, "outside.jar")));
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
        assertTrue(tmpDir.resolve("lib").toFile().mkdir());
        createJarWithManifest(fileToUpload);
        return fileToUpload;
    }

    private static void createJarWithManifest(Path jarPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(jarPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ZipEntry metaInfDir = new ZipEntry("META-INF/");
            zos.putNextEntry(metaInfDir);
            zos.closeEntry();

            ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
            zos.putNextEntry(manifestEntry);

            zos.write("test".getBytes());
            zos.closeEntry();
        }
    }
}
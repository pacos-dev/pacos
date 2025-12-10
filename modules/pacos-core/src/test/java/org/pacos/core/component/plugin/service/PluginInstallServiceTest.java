package org.pacos.core.component.plugin.service;

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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PluginInstallServiceTest {

    @InjectMocks
    private PluginInstallService pluginInstallService;
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
            pluginInstallService.storePluginFile(info);
        }
        assertTrue(tmpDir.resolve("lib").resolve(pluginDTO.toArtifact().getJarPath()).toFile().exists());
    }


    @Test
    void whenFileExistsAndCantBeDeletedThenThrowException() throws IOException {
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
            assertThrows(PacosException.class, () -> pluginInstallService.storePluginFile(info));
        }
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
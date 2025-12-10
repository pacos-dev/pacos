package org.pacos.core.component.plugin.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.base.exception.PacosException;
import org.pacos.core.component.plugin.dto.PluginDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginMetaInfoReaderTest {

    @TempDir
    private Path tmpDir;

    @Test
    void whenManifestValidThenReturnPluginDTO() throws IOException {
        String manifestContent =
                """
                        Manifest-Version: 1.0
                        Implementation-Vendor: ExampleVendor
                        Name: ExamplePlugin
                        Icon: example-icon.png
                        Implementation-Version: 1.0.0
                        Implementation-Group: org.example
                        Implementation-Title: example-artifact
                        """;
        Path jarPath = tmpDir.resolve("test_file.jar");

        createJarWithManifest(jarPath, manifestContent);

        try (InputStream fileStream = new FileInputStream(jarPath.toString())) {
            byte[] fileBytes = fileStream.readAllBytes();
            PluginDTO pluginDTO = PluginMetaInfoReader.read("test_jar", fileBytes);
            assertEquals("1.0.0", pluginDTO.getVersion());
            assertEquals("org.example", pluginDTO.getGroupId());
            assertEquals("example-artifact", pluginDTO.getArtifactName());
            assertEquals("example-icon.png", pluginDTO.getIcon());
            assertEquals("ExamplePlugin", pluginDTO.getName());
            assertEquals("ExampleVendor", pluginDTO.getAuthor());
        }
    }

    @Test
    void whenManifestCorruptedThenThrowException() throws IOException {
        String manifestContent =
                """
                         Manifest-Version: 1.0
                        Implementation-Vendor: ExampleVendor
                        Name: ExamplePlugin
                        Icon: example-icon.png
                        Implementation-Version: 1.0.0
                        Implementation-Group: org.example
                        Implementation-Title: example-artifact
                        """;
        Path jarPath = tmpDir.resolve("test_file.jar");

        createJarWithManifest(jarPath, manifestContent);

        try (InputStream fileStream = new FileInputStream(jarPath.toString())) {
            byte[] fileBytes = fileStream.readAllBytes();
            assertThrows(PacosException.class, () -> PluginMetaInfoReader.read("test_jar", fileBytes));

        }
    }

    @Test
    void whenVersionIsMissingThenThrowException() throws IOException {
        String manifestContent =
                """
                        Manifest-Version: 1.0
                        Implementation-Vendor: ExampleVendor
                        Name: ExamplePlugin
                        Icon: example-icon.png
                        Implementation-Group: org.example
                        Implementation-Title: example-artifact
                        """;
        Path jarPath = tmpDir.resolve("test_file.jar");

        createJarWithManifest(jarPath, manifestContent);

        try (InputStream fileStream = new FileInputStream(jarPath.toString())) {
            byte[] fileBytes = fileStream.readAllBytes();
            assertThrows(PacosException.class, () -> PluginMetaInfoReader.read("test_jar", fileBytes));

        }
    }

    @Test
    void whenGroupIsMissingThenThrowException() throws IOException {
        String manifestContent =
                """
                        Manifest-Version: 1.0
                        Implementation-Vendor: ExampleVendor
                        Name: ExamplePlugin
                        Icon: example-icon.png
                        Implementation-Version: 1.0.0
                        Implementation-Title: example-artifact
                        """;
        Path jarPath = tmpDir.resolve("test_file.jar");

        createJarWithManifest(jarPath, manifestContent);

        try (InputStream fileStream = new FileInputStream(jarPath.toString())) {
            byte[] fileBytes = fileStream.readAllBytes();
            assertThrows(PacosException.class, () -> PluginMetaInfoReader.read("test_jar", fileBytes));

        }
    }

    @Test
    void whenTitleIsMissingThenThrowException() throws IOException {
        String manifestContent =
                """
                        Manifest-Version: 1.0
                        Implementation-Vendor: ExampleVendor
                        Name: ExamplePlugin
                        Icon: example-icon.png
                        Implementation-Group: org.example
                        """;
        Path jarPath = tmpDir.resolve("test_file.jar");

        createJarWithManifest(jarPath, manifestContent);

        try (InputStream fileStream = new FileInputStream(jarPath.toString())) {
            byte[] fileBytes = fileStream.readAllBytes();
            assertThrows(PacosException.class, () -> PluginMetaInfoReader.read("test_jar", fileBytes));

        }
    }

    @Test
    void whenManifestIsMissingThenThrowException() throws IOException {
        Path jarPath = tmpDir.resolve("test_file.jar");

        createJar(jarPath);

        try (InputStream fileStream = new FileInputStream(jarPath.toString())) {
            byte[] fileBytes = fileStream.readAllBytes();
            assertThrows(PacosException.class, () -> PluginMetaInfoReader.read("test_jar", fileBytes));

        }
    }

    private void createJar(Path jarPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(jarPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ZipEntry metaInfDir = new ZipEntry("META-INF/");
            zos.putNextEntry(metaInfDir);
            zos.closeEntry();
        }
    }

    private static void createJarWithManifest(Path jarPath, String manifestContent) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(jarPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ZipEntry metaInfDir = new ZipEntry("META-INF/");
            zos.putNextEntry(metaInfDir);
            zos.closeEntry();

            ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
            zos.putNextEntry(manifestEntry);

            zos.write(manifestContent.getBytes());
            zos.closeEntry();
        }
    }
}

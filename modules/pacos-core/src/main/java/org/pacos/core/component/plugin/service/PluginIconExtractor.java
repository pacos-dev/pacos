package org.pacos.core.component.plugin.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.pacos.config.property.WorkingDir;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for extracting icon from module jar file
 */
public class PluginIconExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(PluginIconExtractor.class);

    private PluginIconExtractor() {
    }

    public static void extractIcon(PluginDTO pluginDTO) {
        LOG.info("Extracting plugin icon {}", pluginDTO);
        File jarFile = WorkingDir.getLibPath().resolve(pluginDTO.toArtifact().getJarPath()).toFile();
        if (!jarFile.exists()) {
            LOG.error("Plugin jar file does not exist");
            return;
        }
        String iconPath = readIconPathFromManifest(jarFile);
        if (iconPath == null) {
            LOG.error("Plugin jar file does not contain icon path");
            return;
        }
        Path iconDirPath = WorkingDir.getModulePath(pluginDTO.getArtifactName()).resolve("icon");
        if (!iconDirPath.toFile().exists() && !iconDirPath.toFile().mkdirs()) {
            LOG.error("Unable to create icon directory for plugin {}", iconDirPath);
            return;
        }
        iconDirPath = iconDirPath.resolve(pluginDTO.getArtifactName() + ".png");

        readIconFromJarFile(jarFile, iconPath, iconDirPath);
    }

    private static String readIconPathFromManifest(File jarFile) {
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(jarFile))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if ("META-INF/MANIFEST.MF".equals(entry.getName())) {

                    Manifest manifest = new Manifest(zipStream);
                    return "META-INF/resources/" + manifest.getMainAttributes().getValue("Icon");
                }
            }
            LOG.error("MANIFEST.MF not found");
        } catch (Exception e) {
            LOG.error("META-INF/MANIFEST.MF file not found in given plugin file {}", jarFile);
        }
        return null;
    }

    private static void readIconFromJarFile(File file, String iconLocation, Path iconDestination) {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry entry = jarFile.getJarEntry(iconLocation);
            if (entry != null) {
                File outputFile = iconDestination.toFile();
                extractIcon(file, outputFile, jarFile, entry);
            }
        } catch (Exception e) {
            LOG.error("Error reading icon from jar file {}", file, e);
        }
    }

    private static void extractIcon(File file, File outputFile, JarFile jarFile, JarEntry entry) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            var inputStream = jarFile.getInputStream(entry);
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            LOG.error("Error reading icon from jar file {}", file, e);
        }
    }
}

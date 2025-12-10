package org.pacos.core.component.plugin.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.pacos.core.component.plugin.manager.data.PluginJar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for extracting static resource list from module jar file
 */
public class PluginResourceReader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginResourceReader.class);

    private PluginResourceReader() {
    }

    public static Set<String> readResourceList(PluginJar pluginJar) {
        LOG.info("Reading plugin resources {}", pluginJar.getLibPath());
        File jarFile = pluginJar.getLibPath().toFile();
        if (!jarFile.exists()) {
            LOG.error("Plugin jar file does not exist");
            return Set.of();
        }
        return readResourceList(jarFile);
    }

    private static Set<String> readResourceList(File jarFile) {
        Set<String> resources = new HashSet<>();
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(jarFile))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName().startsWith("META-INF/resources") && !entry.isDirectory()) {
                    resources.add(entry.getName().replace("META-INF/resources", ""));
                }
            }
        } catch (Exception e) {
            LOG.error("Can't load resources list from META-INF/resources {}", jarFile);
        }
        return resources;
    }

}

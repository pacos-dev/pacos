package org.pacos.core.component.plugin.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.pacos.base.exception.PacosException;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reads MANIFEST from given jar file plugin uploaded by the user from the UI
 */
public final class PluginMetaInfoReader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginMetaInfoReader.class);

    private PluginMetaInfoReader() {
    }

    public static PluginDTO read(String fileName, byte[] fileData) throws IOException {
        LOG.info("Starting read uploaded plugin {}", fileName);

        try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(fileData))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if ("META-INF/MANIFEST.MF".equals(entry.getName())) {

                    Manifest manifest = new Manifest(zipStream);
                    PluginDTO pluginDTO = getPluginDTO(manifest);

                    if (pluginDTO.getVersion() == null || pluginDTO.getGroupId() == null || pluginDTO.getArtifactName() == null) {
                        LOG.error("Basic manifest data not found in file {}", fileName);
                        throw new PacosException("MANIFEST.MF for given file '" + fileName + "' is corrupted");
                    }
                    return pluginDTO;
                }
            }
        } catch (IOException e) {
            LOG.error("META-INF/MANIFEST.MF file not found in given file {}", fileName);
            throw new PacosException("Given file '" + fileName + "' is invalid. Can't find META-INF/MANIFEST.MF");
        }
        LOG.error("META-INF/MANIFEST.MF file not found in given file {}", fileName);
        throw new PacosException("Given file '" + fileName + "' is invalid. Can't find META-INF/MANIFEST.MF");
    }

    private static PluginDTO getPluginDTO(Manifest manifest) {
        Attributes mainAttributes = manifest.getMainAttributes();

        PluginDTO pluginDTO = new PluginDTO();

        pluginDTO.setAuthor(mainAttributes.getValue("Implementation-Vendor"));
        pluginDTO.setName(mainAttributes.getValue("Name"));
        pluginDTO.setIcon(mainAttributes.getValue("Icon"));

        pluginDTO.setVersion(mainAttributes.getValue("Implementation-Version"));
        pluginDTO.setGroupId(mainAttributes.getValue("Implementation-Group"));
        pluginDTO.setArtifactName(mainAttributes.getValue("Implementation-Title"));
        return pluginDTO;
    }

}

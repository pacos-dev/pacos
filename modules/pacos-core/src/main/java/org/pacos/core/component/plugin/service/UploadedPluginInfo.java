package org.pacos.core.component.plugin.service;

import java.util.Objects;

import org.pacos.core.component.plugin.dto.PluginDTO;

/**
 * Contains information about uploaded plugin by the User in UI
 */
public record UploadedPluginInfo(PluginDTO pluginDTO, byte[] fileData, String fileName) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UploadedPluginInfo that = (UploadedPluginInfo) o;
        return Objects.equals(fileName(), that.fileName()) && Objects.equals(pluginDTO(), that.pluginDTO());
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginDTO(), fileName());
    }

    @Override
    public String toString() {
        return "UploadedPluginInfo{" +
                "pluginDTO=" + pluginDTO +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}

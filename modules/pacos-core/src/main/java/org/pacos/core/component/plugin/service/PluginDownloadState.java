package org.pacos.core.component.plugin.service;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.view.plugin.DownloadPluginStatus;

/**
 * Represents current state of download process for given plugin
 */
public record PluginDownloadState(PluginDTO pluginDTO, DownloadPluginStatus status) {
}

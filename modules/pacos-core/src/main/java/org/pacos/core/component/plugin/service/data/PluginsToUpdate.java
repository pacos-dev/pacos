package org.pacos.core.component.plugin.service.data;

import java.util.List;

import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.plugin.dto.PluginDTO;

/**
 * Contains list with plugin to update
 */
public record PluginsToUpdate(List<PluginDTO> plugins, AppRepository repository) {
}

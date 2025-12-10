package org.pacos.core.component.plugin.manager.data;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;

public record PluginStatus(PluginDTO pluginDTO, PluginStatusEnum pluginState) {

}

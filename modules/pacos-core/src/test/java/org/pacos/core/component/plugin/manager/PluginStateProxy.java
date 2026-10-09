package org.pacos.core.component.plugin.manager;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;

public class PluginStateProxy {
    public static void setState(PluginState pluginState, PluginDTO plugin, PluginStatusEnum pluginStateEnum) {
        pluginState.setState(plugin, pluginStateEnum);
    }
}

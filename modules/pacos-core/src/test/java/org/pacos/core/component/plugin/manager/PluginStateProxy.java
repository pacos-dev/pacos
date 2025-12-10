package org.pacos.core.component.plugin.manager;

import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;

public class PluginStateProxy {

    public static void setState(PluginDTO plugin, PluginStatusEnum pluginStateEnum){
        PluginState.setState(plugin, pluginStateEnum);
    }
}

package org.pacos.core.component.plugin.event;

import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;

public class SystemRestartRequiredEvent {
    private SystemRestartRequiredEvent() {
    }

    public static void fireEvent(RegistryProxy registryProxy) {
        registryProxy.saveRegistry(RegistryName.RESTART_REQUIRED, true);
        UISystem.getCurrent().notify(ModuleEvent.RESTART_REQUIRED);
    }
}

package org.pacos.core.component.installer.service;

import org.pacos.base.session.UserDTO;
import org.pacos.core.component.dock.service.DockService;
import org.pacos.core.component.settings.view.PanelSettings;
import org.pacos.core.component.variable.view.PanelVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DockConfiguration {

    private final DockService dockService;

    @Autowired
    public DockConfiguration(DockService dockService) {
        this.dockService = dockService;
    }

    @Transactional("coreTransactionManager")
    public void configure(UserDTO user) {
        dockService.addActivator(PanelSettings.class.getSimpleName(), user.getId());
        dockService.addActivator(PanelVariable.class.getSimpleName(), user.getId());
    }

}

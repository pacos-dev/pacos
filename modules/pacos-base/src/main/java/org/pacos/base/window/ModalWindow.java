package org.pacos.base.window;

import org.pacos.base.window.config.impl.ModalWindowConfig;

/**
 * Basic window implementation not managed by spring. Configuration is provided directly through the constructor.
 */
public class ModalWindow extends DesktopWindow {

    public ModalWindow(ModalWindowConfig moduleConfig) {
        super(moduleConfig, moduleConfig.getContent());
        moduleConfig.getContent().setPadding(true);
        allowCloseOnEsc();
        moduleConfig.getWindowState().apply(this);
    }
}

package org.pacos.base.window.config.impl;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.event.OnConfirmEvent;

/**
 * Provides basic confirmation window dialog
 */
public class ConfirmationWindowConfig extends ModalWindowConfig {

    public ConfirmationWindowConfig(OnConfirmEvent onConfirmEvent) {
        setIcon(PacosIcon.QUESTION.getUrl());
        setTitle("Confirmation");
        getWindowState().withConfirmEvent(onConfirmEvent);
    }

}

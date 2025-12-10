package org.pacos.base.window.config.impl;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.WarningWindow;
import org.pacos.base.window.WindowState;
import org.pacos.base.window.config.WindowConfig;

/**
 * Configuration for modal warning window
 */
public class WarningWindowConfig implements WindowConfig {

    private final String message;
    private final String title;
    private final WindowState windowState;

    public WarningWindowConfig(String title, String message) {
        this.message = message;
        this.title = title;
        this.windowState = new WindowState()
                .withMinimizeAllowed(false)
                .withModal(true)
                .withResizable(true);
    }

    @Override
    public String title() {
        return "Warning";
    }

    @Override
    public String icon() {
        return PacosIcon.ALERT.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return WarningWindow.class;
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public boolean isAllowMultipleInstance() {
        return true;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public WindowState getWindowState() {
        return windowState;
    }
}

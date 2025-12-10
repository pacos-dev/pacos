package org.pacos.base.window.config.impl;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.ModalWindow;
import org.pacos.base.window.WindowState;
import org.pacos.base.window.config.WindowConfig;

/**
 * Basic implementation of ModuleConfig for {@link ModalWindow}.
 * Content must be set before this class will be used by {@link org.pacos.base.window.manager.WindowManager}
 */
public class ModalWindowConfig implements WindowConfig {

    private VerticalLayout content = new VerticalLayout();
    private String icon;
    private String title = "Modal window";

    private boolean allowMultipleInstance = true;
    private final WindowState windowState = new WindowState();

    @Override
    public String title() {
        return title;
    }

    @Override
    public String icon() {
        return icon;
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return ModalWindow.class;
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public boolean isAllowMultipleInstance() {
        return allowMultipleInstance;
    }

    @Override
    public boolean isAllowedForMinimize() {return false;}

    public void setContent(VerticalLayout content) {
        this.content = content;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAllowMultipleInstance(boolean allowMultipleInstance) {
        this.allowMultipleInstance = allowMultipleInstance;
    }

    public VerticalLayout getContent() {
        return content;
    }

    public WindowState getWindowState() {
        return windowState;
    }
}

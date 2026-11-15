package org.pacos.core.system.view.config;

import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.system.window.OnboardingWindow;
import org.springframework.stereotype.Component;

/**
 * Window configuration for the first-run PacOS guide.
 */
@Component
public class OnboardingWindowConfig implements WindowConfig {

    @Override
    public String title() {
        return "Getting started with PacOS";
    }

    @Override
    public String icon() {
        return PacosIcon.APP_STORE.getUrl();
    }

    @Override
    public Class<? extends DesktopWindow> activatorClass() {
        return OnboardingWindow.class;
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public boolean isAllowMultipleInstance() {
        return false;
    }
}

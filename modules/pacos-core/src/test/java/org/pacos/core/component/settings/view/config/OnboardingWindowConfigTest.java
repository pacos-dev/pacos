package org.pacos.core.component.settings.view.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.core.system.window.OnboardingWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OnboardingWindowConfigTest {

    private final OnboardingWindowConfig config = new OnboardingWindowConfig();

    @Test
    void whenGetTitleThenReturnGettingStartedTitle() {
        assertEquals("Getting started with PacOS", config.title());
    }

    @Test
    void whenGetIconThenReturnOnboardingIcon() {
        assertEquals(PacosIcon.ONBOARD.getUrl(), config.icon());
    }

    @Test
    void whenGetActivatorClassThenReturnOnboardingWindow() {
        assertEquals(OnboardingWindow.class, config.activatorClass());
    }

    @Test
    void whenIsApplicationThenReturnFalse() {
        assertFalse(config.isApplication());
    }

    @Test
    void whenMultipleInstancesAreCheckedThenReturnFalse() {
        assertFalse(config.isAllowMultipleInstance());
    }
}

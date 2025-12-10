package org.pacos.base.window.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WindowConfigTest {


    private WindowConfig moduleConfig;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        moduleConfig = mock(WindowConfig.class);
        userSession = mock(UserSession.class);
    }

    @Test
    void whenTitleCalledThenReturnExpectedTitle() {
        when(moduleConfig.title()).thenReturn("Test Title");
        assertEquals("Test Title", moduleConfig.title());
    }

    @Test
    void whenIconCalledThenReturnExpectedIcon() {
        when(moduleConfig.icon()).thenReturn("test-icon.png");
        assertEquals("test-icon.png", moduleConfig.icon());
    }

    @Test
    void whenIsApplicationCalledThenReturnTrue() {
        when(moduleConfig.isApplication()).thenReturn(true);
        assertTrue(moduleConfig.isApplication());
    }

    @Test
    void whenIsAllowMultipleInstanceCalledThenReturnTrue() {
        when(moduleConfig.isAllowMultipleInstance()).thenReturn(true);
        assertTrue(moduleConfig.isAllowMultipleInstance());
    }

    @Test
    void whenIsAllowedForCurrentSessionCalledThenReturnTrueByDefault() {
        // Using the default implementation
        when(moduleConfig.isAllowedForCurrentSession(userSession)).thenCallRealMethod();
        assertTrue(moduleConfig.isAllowedForCurrentSession(userSession));
    }

    @Test
    void whenIsAllowedForMinimizeCalledThenReturnTrueByDefault() {
        // Using the default implementation
        when(moduleConfig.isAllowedForMinimize()).thenCallRealMethod();
        assertTrue(moduleConfig.isAllowedForMinimize());
    }
}
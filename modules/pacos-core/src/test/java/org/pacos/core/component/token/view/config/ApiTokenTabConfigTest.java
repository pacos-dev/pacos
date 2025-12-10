package org.pacos.core.component.token.view.config;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.token.service.ApiTokenService;
import org.pacos.core.component.token.view.ApiConfigTabLayout;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiTokenTabConfigTest {

    private ApiTokenTabConfig config;

    @BeforeEach
    void setUp() {
        ApiTokenService tokenService = mock(ApiTokenService.class);
        config = new ApiTokenTabConfig(tokenService);
    }

    @Test
    void whenCallGetTitleThenReturnApiAccess() {
        String title = config.getTitle();
        assertEquals("API access", title);
    }

    @Test
    void whenCallGenerateContentThenReturnApiConfigTabLayout() {
        VerticalLayout layout = config.generateContent();
        assertTrue(layout instanceof ApiConfigTabLayout);
    }

    @Test
    void whenCallGetOrderThenReturnZero() {
        int order = config.getOrder();
        assertEquals(0, order);
    }

    @Test
    void whenCallShouldBeDisplayedWithGuestUserThenReturnFalse() {
        UserSession userSession = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        when(userSession.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(true);

        boolean result = config.shouldBeDisplayed(userSession);

        assertFalse(result);
    }

    @Test
    void whenCallShouldBeDisplayedWithNonGuestUserThenReturnTrue() {
        UserSession userSession = mock(UserSession.class);
        UserDTO user = mock(UserDTO.class);
        when(userSession.getUser()).thenReturn(user);
        when(user.isGuestSession()).thenReturn(false);
        when(userSession.hasPermission(SystemPermissions.API_TAB_VISIBLE)).thenReturn(true);
        boolean result = config.shouldBeDisplayed(userSession);

        assertTrue(result);
    }

    @Test
    void whenOnSaveThenNoException() {
        assertDoesNotThrow(() -> config.generateContent().onShortCutDetected(ShortcutType.SAVE));
    }

    @Test
    void whenOnDeleteThenNoException() {
        assertDoesNotThrow(() -> config.generateContent().onShortCutDetected(ShortcutType.DELETE));
    }
}

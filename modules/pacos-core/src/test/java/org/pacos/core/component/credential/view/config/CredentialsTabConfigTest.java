package org.pacos.core.component.credential.view.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.credential.view.CredentialsTabLayout;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.security.service.RoleService;

class CredentialsTabConfigTest {

    private CredentialsTabConfig config;
    private CredentialVaultService credentialService;
    private RoleService roleService;
    private PluginProxy pluginProxy;
    @BeforeEach
    void setUp() {
        credentialService = mock(CredentialVaultService.class);
        roleService = mock(RoleService.class);
        pluginProxy = mock(PluginProxy.class);
        roleService = mock(RoleService.class);
        when(credentialService.getAvailableTypes()).thenReturn(java.util.List.of());
        when(credentialService.findAvailable(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());
        config = new CredentialsTabConfig(credentialService, roleService, pluginProxy, 10 * 1024 * 1024L);
        VaadinMock.mockSystem();
    }

    @Test
    void whenGetTitleCalledThenReturnCredentials() {
        assertEquals("Credentials", config.getTitle());
    }

    @Test
    void whenGetOrderCalledThenReturnTen() {
        assertEquals(10, config.getOrder());
    }

    @Test
    void whenShouldBeDisplayedForAuthenticatedUserThenReturnTrue() {
        UserSession session = mock(UserSession.class);
        when(session.hasPermission(SystemPermissions.CREDENTIALS_TAB_VISIBLE)).thenReturn(true);

        assertTrue(config.shouldBeDisplayed(session));
    }

    @Test
    void whenShouldBeDisplayedForGuestThenReturnFalse() {
        UserSession session = mock(UserSession.class);
        when(session.hasPermission(SystemPermissions.CREDENTIALS_TAB_VISIBLE)).thenReturn(false);

        assertFalse(config.shouldBeDisplayed(session));
    }

    @Test
    void whenGenerateContentCalledThenReturnCredentialsLayout() {
        when(roleService.loadAllRoles()).thenReturn(java.util.List.of());

        assertInstanceOf(CredentialsTabLayout.class, config.generateContent());
    }
}

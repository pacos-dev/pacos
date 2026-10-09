package org.pacos.core.component.plugin.view.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.view.tab.PluginManagementTabLayout;
import org.pacos.core.component.security.SystemPermissions;

class PluginManagementConfigTest {

    private PluginManagementConfig config;
    private PluginProxy pluginProxy;

    @BeforeEach
    void setUp() {
        PluginManager pluginManager = mock(PluginManager.class);
        pluginProxy = mock(PluginProxy.class);
        config = new PluginManagementConfig(pluginManager, pluginProxy);
        VaadinMock.mockSystem();
    }

    @Test
    void whenGetTitleIsCalledThenReturnsPluginManagement() {
        //given

        //when
        String title = config.getTitle();

        //then
        assertEquals("Plugin management", title);
    }

    @Test
    void whenGenerateContentIsCalledThenReturnsPluginManagementTabLayoutInstance() {
        //given
        when(pluginProxy.getPluginState()).thenReturn(mock(PluginState.class));
        //when
        SettingPageLayout content = config.generateContent();

        //then
        assertNotNull(content);
        assertInstanceOf(PluginManagementTabLayout.class, content);
    }

    @Test
    void whenGetOrderIsCalledThenReturnsSeventyOne() {
        //given

        //when
        int order = config.getOrder();

        //then
        assertEquals(71, order);
    }

    @Test
    void whenUserHasPermissionThenShouldBeDisplayedReturnsTrue() {
        //given

        when(UserSession.getCurrent().hasPermission(SystemPermissions.PLUGIN_MANAGEMENT_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = config.shouldBeDisplayed(mock(UserSession.class));

        //then
        assertTrue(result);
    }

    @Test
    void whenUserLacksPermissionThenShouldBeDisplayedReturnsFalse() {
        //given
        when(UserSession.getCurrent().hasPermission(SystemPermissions.PLUGIN_MANAGEMENT_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = config.shouldBeDisplayed(mock(UserSession.class));

        //then
        assertFalse(result);
    }

    @Test
    void whenGetSearchIndexIsCalledThenReturnNotNullValue() {
        assertNotNull(PluginManagementTabLayout.getSearchIndex());
    }
}
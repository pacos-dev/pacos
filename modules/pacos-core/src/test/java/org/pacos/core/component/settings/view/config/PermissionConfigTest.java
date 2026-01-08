package org.pacos.core.component.settings.view.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PermissionConfigTest {

    private PermissionConfig permissionConfig;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        permissionConfig = new PermissionConfig();
        userSession = Mockito.mock(UserSession.class);
    }

    @Test
    void whenGetTitleIsCalledThenReturnsCorrectTabName() {
        //given
        String expectedTitle = SettingTabName.PERMISSIONS.getName();

        //when
        String actualTitle = permissionConfig.getTitle();

        //then
        assertEquals(expectedTitle, actualTitle);
    }

    @Test
    void whenGenerateContentIsCalledThenReturnsPermissionConfigViewInstance() {
        //given

        //when
        SettingPageLayout content = permissionConfig.generateContent();

        //then
        assertNotNull(content);
        assertInstanceOf(PermissionConfigView.class, content);
    }

    @Test
    void whenGetOrderIsCalledThenReturnsOneHundredFifty() {
        //given

        //when
        int order = permissionConfig.getOrder();

        //then
        assertEquals(150, order);
    }

    @Test
    void whenUserHasUserPermissionsTabVisibleThenShouldBeDisplayedReturnsTrue() {
        //given
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(true);
        when(userSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = permissionConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserHasDefaultPermissionsTabVisibleThenShouldBeDisplayedReturnsTrue() {
        //given
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);
        when(userSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = permissionConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserHasNoRequiredPermissionsThenShouldBeDisplayedReturnsFalse() {
        //given
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);
        when(userSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = permissionConfig.shouldBeDisplayed(userSession);

        //then
        assertFalse(result);
    }
}
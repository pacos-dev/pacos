package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.springframework.context.ApplicationContext;

class RolesManagementTabConfigTest {

    private RolesManagementTabConfig rolesManagementTabConfig;
    private ApplicationContext context;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        context = mock(ApplicationContext.class);
        userSession = mock(UserSession.class);
        rolesManagementTabConfig = new RolesManagementTabConfig(context);
    }

    @Test
    void whenGetTitleCalledThenReturnRolesName() {
        //when
        String result = rolesManagementTabConfig.getTitle();

        //then
        assertEquals(SettingTabName.ROLES.getName(), result);
    }

    @Test
    void whenGenerateContentCalledThenReturnRolesManagementTabViewBean() {
        //given
        RolesManagementTabView expectedView = mock(RolesManagementTabView.class);
        when(context.getBean(RolesManagementTabView.class)).thenReturn(expectedView);

        //when
        SettingPageLayout result = rolesManagementTabConfig.generateContent();

        //then
        assertEquals(expectedView, result);
    }

    @Test
    void whenGetGroupCalledThenReturnAccessManagementGroup() {
        //when
        String[] result = rolesManagementTabConfig.getGroup();

        //then
        assertArrayEquals(new String[] { SettingTabName.ACCESS_MANAGEMENT.getName() }, result);
    }

    @Test
    void whenGetOrderCalledThenReturnOneHundredFifty() {
        //when
        int result = rolesManagementTabConfig.getOrder();

        //then
        assertEquals(150, result);
    }

    @Test
    void whenUserHasUserPermissionsTabVisibleThenShouldBeDisplayedReturnsTrue() {
        //given
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = rolesManagementTabConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserHasDefaultPermissionsTabVisibleThenShouldBeDisplayedReturnsTrue() {
        //given
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);
        when(userSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = rolesManagementTabConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserLacksBothPermissionsThenShouldBeDisplayedReturnsFalse() {
        //given
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);
        when(userSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = rolesManagementTabConfig.shouldBeDisplayed(userSession);

        //then
        assertFalse(result);
    }
}
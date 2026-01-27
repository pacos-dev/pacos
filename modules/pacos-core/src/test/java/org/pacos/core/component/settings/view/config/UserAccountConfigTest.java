package org.pacos.core.component.settings.view.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.config.access.UserAccountConfig;
import org.springframework.context.ApplicationContext;

class UserAccountConfigTest {

    private UserAccountConfig userAccountConfig;

    @BeforeEach
    void setUp() {
        ApplicationContext context = mock(ApplicationContext.class);
        userAccountConfig = new UserAccountConfig(context);
    }

    @Test
    void whenGetTitleIsCalledThenReturnsUserPermissions() {
        //given

        //when
        String title = userAccountConfig.getTitle();

        //then
        assertEquals(SettingTabName.USER.getName(), title);
    }

    @Test
    void whenGetOrderIsCalledThenReturnsZero() {
        //given

        //when
        int order = userAccountConfig.getOrder();

        //then
        assertEquals(0, order);
    }

    @Test
    void whenUserHasPermissionThenShouldBeDisplayedReturnsTrue() {
        //given
        UserSession userSession = mock(UserSession.class);
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = userAccountConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserLacksPermissionThenShouldBeDisplayedReturnsFalse() {
        //given
        UserSession userSession = mock(UserSession.class);
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = userAccountConfig.shouldBeDisplayed(userSession);

        //then
        assertFalse(result);
    }

    @Test
    void whenGetGroupIsCalledThenReturnsPermissionsGroupName() {
        //given

        //when
        String[] group = userAccountConfig.getGroup();

        //then
        assertArrayEquals(new String[] { SettingTabName.ACCESS_MANAGEMENT.getName() }, group);
    }

}
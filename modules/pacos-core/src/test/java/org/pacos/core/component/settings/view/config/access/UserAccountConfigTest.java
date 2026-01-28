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

class UserAccountConfigTest {

    private UserAccountConfig userAccountConfig;
    private ApplicationContext context;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        context = mock(ApplicationContext.class);
        userSession = mock(UserSession.class);
        userAccountConfig = new UserAccountConfig(context);
    }

    @Test
    void whenGetTitleCalledThenReturnUserName() {
        //when
        String result = userAccountConfig.getTitle();

        //then
        assertEquals(SettingTabName.USER.getName(), result);
    }

    @Test
    void whenGenerateContentCalledThenReturnUserAccountsTabLayoutBean() {
        //given
        UserAccountsTabLayout expectedLayout = mock(UserAccountsTabLayout.class);
        when(context.getBean(UserAccountsTabLayout.class)).thenReturn(expectedLayout);

        //when
        SettingPageLayout result = userAccountConfig.generateContent();

        //then
        assertEquals(expectedLayout, result);
    }

    @Test
    void whenGetOrderCalledThenReturnZero() {
        //when
        int result = userAccountConfig.getOrder();

        //then
        assertEquals(0, result);
    }

    @Test
    void whenUserHasPermissionThenShouldBeDisplayedReturnsTrue() {
        //given
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = userAccountConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserLacksPermissionThenShouldBeDisplayedReturnsFalse() {
        //given
        when(userSession.hasPermission(SystemPermissions.USER_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = userAccountConfig.shouldBeDisplayed(userSession);

        //then
        assertFalse(result);
    }

    @Test
    void whenGetGroupCalledThenReturnAccessManagementGroup() {
        //when
        String[] result = userAccountConfig.getGroup();

        //then
        assertArrayEquals(new String[] { SettingTabName.ACCESS_MANAGEMENT.getName() }, result);
    }

    @Test
    void whenGetSearchIndexCalledThenReturnLayoutIndex() {
        //when
        String result = userAccountConfig.getSearchIndex();

        //then
        assertEquals(UserAccountsTabLayout.getSearchIndex(), result);
    }
}
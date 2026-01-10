package org.pacos.core.component.settings.view.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.UserAccountsTabLayout;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAccountConfigTest {

    private UserAccountConfig userAccountConfig;
    private ApplicationContext context;
    private MockedStatic<UserAccountsTabLayout> layoutMockedStatic;

    @BeforeEach
    void setUp() {
        context = mock(ApplicationContext.class);
        userAccountConfig = new UserAccountConfig(context);
        layoutMockedStatic = mockStatic(UserAccountsTabLayout.class);
    }

    @AfterEach
    void tearDown() {
        layoutMockedStatic.close();
    }

    @Test
    void whenGetTitleIsCalledThenReturnsUserPermissions() {
        //given

        //when
        String title = userAccountConfig.getTitle();

        //then
        assertEquals("User permission", title);
    }

    @Test
    void whenGenerateContentIsCalledThenReturnsBeanFromContext() {
        //given
        UserAccountsTabLayout expectedLayout = mock(UserAccountsTabLayout.class);
        when(context.getBean(UserAccountsTabLayout.class)).thenReturn(expectedLayout);

        //when
        SettingPageLayout result = userAccountConfig.generateContent();

        //then
        assertEquals(expectedLayout, result);
        verify(context).getBean(UserAccountsTabLayout.class);
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
        assertArrayEquals(new String[]{SettingTabName.PERMISSIONS.getName()}, group);
    }

    @Test
    void whenGetSearchIndexIsCalledThenReturnsValueFromLayoutStaticMethod() {
        //given
        String expectedIndex = "user.accounts.index";
        layoutMockedStatic.when(UserAccountsTabLayout::getSearchIndex).thenReturn(expectedIndex);

        //when
        String actualIndex = userAccountConfig.getSearchIndex();

        //then
        assertEquals(expectedIndex, actualIndex);
    }
}
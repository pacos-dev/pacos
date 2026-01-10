package org.pacos.core.component.settings.view.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.DefaultPermissionsTabLayout;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultPermissionConfigTest {

    private DefaultPermissionConfig config;
    private ApplicationContext context;
    private MockedStatic<UserSession> userSessionMockedStatic;
    private MockedStatic<DefaultPermissionsTabLayout> layoutMockedStatic;

    @BeforeEach
    void setUp() {
        context = mock(ApplicationContext.class);
        config = new DefaultPermissionConfig(context);
        userSessionMockedStatic = mockStatic(UserSession.class);
        layoutMockedStatic = mockStatic(DefaultPermissionsTabLayout.class);
    }

    @AfterEach
    void tearDown() {
        userSessionMockedStatic.close();
        layoutMockedStatic.close();
    }

    @Test
    void whenGetTitleIsCalledThenReturnsDefaultPermissions() {
        //given

        //when
        String title = config.getTitle();

        //then
        assertEquals("Default permissions", title);
    }

    @Test
    void whenGenerateContentIsCalledThenReturnsBeanFromContext() {
        //given
        DefaultPermissionsTabLayout expectedLayout = mock(DefaultPermissionsTabLayout.class);
        when(context.getBean(DefaultPermissionsTabLayout.class)).thenReturn(expectedLayout);

        //when
        SettingPageLayout result = config.generateContent();

        //then
        assertEquals(expectedLayout, result);
        verify(context).getBean(DefaultPermissionsTabLayout.class);
    }

    @Test
    void whenGetOrderIsCalledThenReturnsZero() {
        //given

        //when
        int order = config.getOrder();

        //then
        assertEquals(0, order);
    }

    @Test
    void whenUserHasPermissionThenShouldBeDisplayedReturnsTrue() {
        //given
        UserSession currentSession = mock(UserSession.class);
        userSessionMockedStatic.when(UserSession::getCurrent).thenReturn(currentSession);
        when(currentSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = config.shouldBeDisplayed(mock(UserSession.class));

        //then
        assertTrue(result);
    }

    @Test
    void whenUserLacksPermissionThenShouldBeDisplayedReturnsFalse() {
        //given
        UserSession currentSession = mock(UserSession.class);
        userSessionMockedStatic.when(UserSession::getCurrent).thenReturn(currentSession);
        when(currentSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = config.shouldBeDisplayed(mock(UserSession.class));

        //then
        assertFalse(result);
    }

    @Test
    void whenGetGroupIsCalledThenReturnsPermissionsGroupName() {
        //given

        //when
        String[] group = config.getGroup();

        //then
        assertArrayEquals(new String[]{SettingTabName.PERMISSIONS.getName()}, group);
    }

    @Test
    void whenGetSearchIndexIsCalledThenReturnsValueFromLayout() {
        //given
        String expectedIndex = "index_value";
        layoutMockedStatic.when(DefaultPermissionsTabLayout::getSearchIndex).thenReturn(expectedIndex);

        //when
        String actualIndex = config.getSearchIndex();

        //then
        assertEquals(expectedIndex, actualIndex);
    }
}
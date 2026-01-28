package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.springframework.context.ApplicationContext;

class OnboardingConfigTest {

    private OnboardingConfig onboardingConfig;
    private ApplicationContext context;
    private UserSession userSession;
    private MockedStatic<UserSession> mockedUserSession;

    @BeforeEach
    void setUp() {
        context = mock(ApplicationContext.class);
        userSession = mock(UserSession.class);
        onboardingConfig = new OnboardingConfig(context);
        mockedUserSession = mockStatic(UserSession.class);
    }

    @AfterEach
    void tearDown() {
        mockedUserSession.close();
    }

    @Test
    void whenGetTitleThenReturnOnboardingName() {
        //when
        String result = onboardingConfig.getTitle();

        //then
        assertEquals(SettingTabName.ONBOARDING.getName(), result);
    }

    @Test
    void whenGenerateContentThenReturnBeanFromContext() {
        //given
        OnboardingTabLayout expectedLayout = mock(OnboardingTabLayout.class);
        when(context.getBean(OnboardingTabLayout.class)).thenReturn(expectedLayout);

        //when
        SettingPageLayout result = onboardingConfig.generateContent();

        //then
        assertEquals(expectedLayout, result);
    }

    @Test
    void whenGetOrderThenReturnZero() {
        //when
        int result = onboardingConfig.getOrder();

        //then
        assertEquals(0, result);
    }

    @Test
    void whenUserHasPermissionThenShouldBeDisplayedReturnsTrue() {
        //given
        mockedUserSession.when(UserSession::getCurrent).thenReturn(userSession);
        when(userSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = onboardingConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserLacksPermissionThenShouldBeDisplayedReturnsFalse() {
        //given
        mockedUserSession.when(UserSession::getCurrent).thenReturn(userSession);
        when(userSession.hasPermission(SystemPermissions.DEFAULT_PERMISSIONS_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = onboardingConfig.shouldBeDisplayed(userSession);

        //then
        assertFalse(result);
    }

    @Test
    void whenGetGroupThenReturnAccessManagementGroup() {
        //when
        String[] result = onboardingConfig.getGroup();

        //then
        assertArrayEquals(new String[] { SettingTabName.ACCESS_MANAGEMENT.getName() }, result);
    }

    @Test
    void whenGetSearchIndexThenReturnOnboardingTabLayoutIndex() {
        //when
        String result = onboardingConfig.getSearchIndex();

        //then
        assertEquals(OnboardingTabLayout.getSearchIndex(), result);
    }
}
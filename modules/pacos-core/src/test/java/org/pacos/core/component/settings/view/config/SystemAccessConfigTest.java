package org.pacos.core.component.settings.view.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.config.VaadinMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.SystemAccessTabLayout;
import org.springframework.context.ApplicationContext;

class SystemAccessConfigTest {

    private SystemAccessConfig systemAccessConfig;
    private MockedStatic<SystemAccessTabLayout> layoutMockedStatic;

    @BeforeEach
    void setUp() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(SystemAccessTabLayout.class)).thenReturn(mock(SystemAccessTabLayout.class));
        systemAccessConfig = new SystemAccessConfig(applicationContext);
        layoutMockedStatic = mockStatic(SystemAccessTabLayout.class);
    }

    @AfterEach
    void tearDown() {
        layoutMockedStatic.close();
    }

    @Test
    void whenGetTitleIsCalledThenReturnsCorrectNameFromEnum() {
        //given

        //when
        String title = systemAccessConfig.getTitle();

        //then
        assertEquals(SettingTabName.ACCESS_MANAGEMENT.getName(), title);
    }

    @Test
    void whenGenerateContentIsCalledThenReturnsSystemAccessTabLayoutInstance() {
        //given
        VaadinMock.mockSystem();
        //when
        SettingPageLayout content = systemAccessConfig.generateContent();

        //then
        assertNotNull(content);
        assertInstanceOf(SettingPageLayout.class, content);
    }

    @Test
    void whenGetOrderIsCalledThenReturnsOneHundred() {
        //given

        //when
        int order = systemAccessConfig.getOrder();

        //then
        assertEquals(200, order);
    }

    @Test
    void whenUserHasPermissionThenShouldBeDisplayedReturnsTrue() {
        //given
        UserSession userSession = mock(UserSession.class);
        when(userSession.hasPermission(SystemPermissions.SYSTEM_ACCESS_TAB_VISIBLE)).thenReturn(true);

        //when
        boolean result = systemAccessConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserLacksPermissionThenShouldBeDisplayedReturnsFalse() {
        //given
        UserSession userSession = mock(UserSession.class);
        when(userSession.hasPermission(SystemPermissions.SYSTEM_ACCESS_TAB_VISIBLE)).thenReturn(false);

        //when
        boolean result = systemAccessConfig.shouldBeDisplayed(userSession);

        //then
        assertFalse(result);
    }

    @Test
    void whenGetGroupIsCalledThenReturnsSystemGroupName() {
        //given

        //when
        String[] group = systemAccessConfig.getGroup();

        //then
        assertArrayEquals(new String[] {}, group);
    }

    @Test
    void whenGetSearchIndexIsCalledThenReturnsValueFromStaticMethod() {
        //given
        String expectedIndex = "system.access.search.index";
        layoutMockedStatic.when(SystemAccessTabLayout::getSearchIndex).thenReturn(expectedIndex);

        //when
        String actualIndex = systemAccessConfig.getSearchIndex();

        //then
        assertEquals(expectedIndex, actualIndex);
    }
}
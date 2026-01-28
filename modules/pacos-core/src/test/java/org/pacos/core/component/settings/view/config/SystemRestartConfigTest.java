package org.pacos.core.component.settings.view.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.SystemRestartLayout;

class SystemRestartConfigTest {

    private SystemRestartConfig systemRestartConfig;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        systemRestartConfig = new SystemRestartConfig();
        userSession = Mockito.mock(UserSession.class);
    }

    @Test
    void whenGetTitleCalledThenReturnRestartName() {
        //when
        String result = systemRestartConfig.getTitle();

        //then
        assertEquals(SettingTabName.RESTART.getName(), result);
    }

    @Test
    void whenGenerateContentCalledThenReturnSystemRestartLayoutInstance() {
        //when
        VaadinMock.mockSystem();
        SettingPageLayout result = systemRestartConfig.generateContent();

        //then
        assertNotNull(result);
        assertInstanceOf(SystemRestartLayout.class, result);
    }

    @Test
    void whenGetOrderCalledThenReturnOne() {
        //when
        int result = systemRestartConfig.getOrder();

        //then
        assertEquals(1, result);
    }

    @Test
    void whenUserHasPermissionThenShouldBeDisplayedReturnsTrue() {
        //given
        when(userSession.hasPermission(SystemPermissions.SYSTEM_RESTART)).thenReturn(true);

        //when
        boolean result = systemRestartConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserLacksPermissionThenShouldBeDisplayedReturnsFalse() {
        //given
        when(userSession.hasPermission(SystemPermissions.SYSTEM_RESTART)).thenReturn(false);

        //when
        boolean result = systemRestartConfig.shouldBeDisplayed(userSession);

        //then
        assertFalse(result);
    }

    @Test
    void whenGetGroupCalledThenReturnSystemGroupName() {
        //when
        String[] result = systemRestartConfig.getGroup();

        //then
        assertArrayEquals(new String[] { SettingTabName.SYSTEM.getName() }, result);
    }

    @Test
    void whenGetSearchIndexCalledThenReturnLayoutIndex() {
        //when
        String result = systemRestartConfig.getSearchIndex();

        //then
        assertEquals(SystemRestartLayout.getSearchIndex(), result);
    }
}
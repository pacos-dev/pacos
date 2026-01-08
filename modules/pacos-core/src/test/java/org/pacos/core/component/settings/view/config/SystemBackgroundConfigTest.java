package org.pacos.core.component.settings.view.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.component.settings.view.tab.SystemBackgroundLayout;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemBackgroundConfigTest {

    @Mock
    private RegistryProxy registryProxy;

    @Mock
    private UserSession userSession;

    private SystemBackgroundConfig systemBackgroundConfig;

    @BeforeEach
    void setUp() {
        //given
        systemBackgroundConfig = new SystemBackgroundConfig(registryProxy);
    }

    @Test
    void whenGetTitleIsCalledThenReturnSystemBackgroundName() {
        //when
        String result = systemBackgroundConfig.getTitle();

        //then
        assertEquals(SettingTabName.SYSTEM_BACKGROUND.getName(), result);
    }

    @Test
    void whenGenerateContentIsCalledThenReturnSystemBackgroundLayout() {
        //when
        SettingPageLayout result = systemBackgroundConfig.generateContent();

        //then
        assertNotNull(result);
        assertInstanceOf(SystemBackgroundLayout.class, result);
    }

    @Test
    void whenGetOrderIsCalledThenReturnOne() {
        //when
        int result = systemBackgroundConfig.getOrder();

        //then
        assertEquals(1, result);
    }

    @Test
    void whenUserHasPermissionThenReturnTrue() {
        //given
        when(userSession.hasPermission(SystemPermissions.SYSTEM_BACKGROUND_CONFIGURATION)).thenReturn(true);

        //when
        boolean result = systemBackgroundConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenUserDoesNotHavePermissionThenReturnFalse() {
        //given
        when(userSession.hasPermission(SystemPermissions.SYSTEM_BACKGROUND_CONFIGURATION)).thenReturn(false);

        //when
        boolean result = systemBackgroundConfig.shouldBeDisplayed(userSession);

        //then
        assertFalse(result);
    }

    @Test
    void whenGetGroupIsCalledThenReturnSystemGroupName() {
        //when
        String[] result = systemBackgroundConfig.getGroup();

        //then
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(SettingTabName.SYSTEM.getName(), result[0]);
    }

    @Test
    void whenGetSearchIndexIsCalledThenReturnStaticSearchIndexFromLayout() {
        //when
        String result = systemBackgroundConfig.getSearchIndex();

        //then
        assertEquals(SystemBackgroundLayout.getSearchIndex(), result);
    }
}
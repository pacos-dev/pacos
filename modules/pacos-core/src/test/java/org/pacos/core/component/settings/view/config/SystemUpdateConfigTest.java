package org.pacos.core.component.settings.view.config;

import org.config.VaadinMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.tab.SystemUpdateTabLayout;
import org.pacos.core.system.service.AutoUpdateService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemUpdateConfigTest {

    private SystemUpdateConfig systemUpdateConfig;
    private MockedStatic<SystemUpdateTabLayout> layoutMockedStatic;

    @BeforeEach
    void setUp() {
        RegistryProxy registryProxy = mock(RegistryProxy.class);
        when(registryProxy.readRegistry(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, Integer.class, 0)).thenReturn(0);
        AutoUpdateService autoUpdateService = mock(AutoUpdateService.class);
        systemUpdateConfig = new SystemUpdateConfig(registryProxy, autoUpdateService);
        layoutMockedStatic = mockStatic(SystemUpdateTabLayout.class);
    }

    @AfterEach
    void tearDown() {
        layoutMockedStatic.close();
    }

    @Test
    void whenGetTitleIsCalledThenReturnsSystem() {
        //given

        //when
        String title = systemUpdateConfig.getTitle();

        //then
        assertEquals("Update", title);
    }

    @Test
    void whenGenerateContentIsCalledThenReturnsSystemUpdateTabLayoutInstance() {
        //given
        VaadinMock.mockSystem();
        //when
        SettingPageLayout content = systemUpdateConfig.generateContent();

        //then
        assertNotNull(content);
        assertInstanceOf(SystemUpdateTabLayout.class, content);
    }

    @Test
    void whenGetOrderIsCalledThenReturnsOneHundredOne() {
        //given

        //when
        int order = systemUpdateConfig.getOrder();

        //then
        assertEquals(101, order);
    }

    @Test
    void whenShouldBeDisplayedIsCalledThenReturnsTrue() {
        //given
        UserSession userSession = mock(UserSession.class);

        //when
        boolean result = systemUpdateConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenGetSearchIndexIsCalledThenReturnsValueFromStaticMethod() {
        //given
        String expectedIndex = "system_update_index";
        layoutMockedStatic.when(SystemUpdateTabLayout::getSearchIndex).thenReturn(expectedIndex);

        //when
        String actualIndex = systemUpdateConfig.getSearchIndex();

        //then
        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    void whenGetGroupThenReturnSystem() {
        assertEquals("System", systemUpdateConfig.getGroup()[0]);
    }
}
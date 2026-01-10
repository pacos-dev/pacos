package org.pacos.core.component.settings.view.config;

import org.junit.jupiter.api.Test;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.component.setting.SettingTabName;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.InfoBox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SystemConfigTest {

    private final SystemConfig systemConfig = new SystemConfig();

    @Test
    void whenGetTitleIsCalledThenSystemNameIsReturned() {
        //given
        String expectedTitle = SettingTabName.SYSTEM.getName();

        //when
        String actualTitle = systemConfig.getTitle();

        //then
        assertEquals(expectedTitle, actualTitle);
    }

    @Test
    void whenGetOrderIsCalledThenValueOneHundredAndOneIsReturned() {
        //given
        int expectedOrder = 101;

        //when
        int actualOrder = systemConfig.getOrder();

        //then
        assertEquals(expectedOrder, actualOrder);
    }

    @Test
    void whenShouldBeDisplayedIsCalledThenTrueIsReturned() {
        //given
        UserSession userSession = mock(UserSession.class);

        //when
        boolean result = systemConfig.shouldBeDisplayed(userSession);

        //then
        assertTrue(result);
    }

    @Test
    void whenGetSearchIndexIsCalledThenEmptyStringIsReturned() {
        //given
        String expectedIndex = "";

        //when
        String actualIndex = systemConfig.getSearchIndex();

        //then
        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    void whenGenerateContentIsCalledThenLayoutIsCreatedWithComponents() {
        //given
        // SystemConfig object initialized

        //when
        SettingPageLayout layout = systemConfig.generateContent();

        //then
        assertNotNull(layout);
        assertTrue(layout.getChildren().anyMatch(InfoBox.class::isInstance));
    }
}
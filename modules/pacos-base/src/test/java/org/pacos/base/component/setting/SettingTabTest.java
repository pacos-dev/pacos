package org.pacos.base.component.setting;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.shortcut.ShortcutType;

import static org.junit.jupiter.api.Assertions.*;

class SettingTabTest {

    private TestSettingTab settingTab;

    @BeforeEach
    void setUp() {
        settingTab = new TestSettingTab();
    }

    @Test
    void whenShouldBeDisplayedCalledThenReturnTrue() {
        assertTrue(settingTab.shouldBeDisplayed(null));
    }

    @Test
    void whenOnDeleteCalledThenNoExceptionThrown() {
        assertDoesNotThrow(() -> settingTab.generateContent().onShortCutDetected(ShortcutType.DELETE));
    }

    @Test
    void whenOnSaveCalledThenNoExceptionThrown() {
        assertDoesNotThrow(() -> settingTab.generateContent().onShortCutDetected(ShortcutType.SAVE));
    }

    @Test
    void whenGetTitleCalledThenReturnExpectedTitle() {
        assertEquals("Test Title", settingTab.getTitle());
    }

    @Test
    void whenGenerateContentCalledThenReturnExpectedContent() {
        VerticalLayout content = settingTab.generateContent();
        assertNotNull(content);
        assertEquals("Content Layout", content.getId().orElse(null));
    }

    @Test
    void whenGetSearchIndexThenReturnNullByDefault(){
        assertNull(settingTab.getSearchIndex());
    }

    @Test
    void whenGetGroupThenReturnNullByDefault(){
        assertNotNull(settingTab.getGroup());
    }

    @Test
    void whenGetOrderCalledThenReturnExpectedOrder() {
        assertEquals(1, settingTab.getOrder());
    }

    private static class TestSettingTab implements SettingTab {

        @Override
        public String getTitle() {
            return "Test Title";
        }

        @Override
        public SettingPageLayout generateContent() {
            SettingPageLayout layout = new SettingPageLayout() {

                @Override
                public void onShortCutDetected(ShortcutType shortcutType) {
                    //not implemented
                }
            };
            layout.setId("Content Layout");
            return layout;
        }

        @Override
        public int getOrder() {
            return 1;
        }

        @Override
        public boolean shouldBeDisplayed(UserSession userSession) {
            return true;
        }
    }
}
package org.pacos.core.component.settings.view.tab;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.background.PredefinedBackground;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SystemBackgroundLayoutTest {

    @Mock
    private RegistryProxy registryProxy;

    @Mock
    private UISystem uiSystem;

    private MockedStatic<UISystem> uiSystemStatic;
    private MockedStatic<NotificationUtils> notificationUtilsStatic;

    @BeforeEach
    void setUp() {
        //given
        uiSystemStatic = mockStatic(UISystem.class);
        notificationUtilsStatic = mockStatic(NotificationUtils.class);
        uiSystemStatic.when(UISystem::getCurrent).thenReturn(uiSystem);
    }

    @AfterEach
    void tearDown() {
        uiSystemStatic.close();
        notificationUtilsStatic.close();
    }

    @Test
    void whenGetSearchIndexIsCalledThenReturnExpectedStrings() {
        //given
        String expected = "Choose one of the preinstalled wallpapers" +
                "Provide a URL to an existing image" +
                "Enable a daily-changing random wallpaper powered by Bing.";

        //when
        String result = SystemBackgroundLayout.getSearchIndex();

        //then
        assertEquals(expected, result);
    }

    @Test
    void whenPredefinedBackgroundSelectedThenRegistryUpdatedAndEventFired() {
        //given
        SystemBackgroundLayout layout = new SystemBackgroundLayout(registryProxy);
        PredefinedBackground background = PredefinedBackground.values()[0];
        String expectedUrl = background.getSrc();

        //when
        layout.getElement().executeJs("this.updateImage('" + expectedUrl + "')");
        invokeUpdateImage(layout, expectedUrl);

        //then
        verify(registryProxy).saveRegistry(RegistryName.BACKGROUND, expectedUrl);
        verify(uiSystem).notify(ModuleEvent.BACKGROUND_CHANGED, expectedUrl);
        notificationUtilsStatic.verify(() -> NotificationUtils.success("Wallpaper updated"));
    }

    @Test
    void whenBingWallpaperEnabledThenRegistryUpdatedWithBingUrl() {
        //given
        SystemBackgroundLayout layout = new SystemBackgroundLayout(registryProxy);
        String bingUrl = "https://bing.biturl.top/?resolution=1920x1080&format=image&index=0";

        //when
        invokeUpdateImage(layout, bingUrl);

        //then
        verify(registryProxy).saveRegistry(RegistryName.BACKGROUND, bingUrl);
        verify(uiSystem).notify(ModuleEvent.BACKGROUND_CHANGED, bingUrl);
        notificationUtilsStatic.verify(() -> NotificationUtils.success("Random daily wallpaper enabled"));
    }

    @Test
    void whenCustomUrlProvidedThenRegistryUpdatedWithManualValue() {
        //given
        SystemBackgroundLayout layout = new SystemBackgroundLayout(registryProxy);
        String customUrl = "https://my-cool-wallpaper.com/img.png";

        //when
        invokeUpdateImage(layout, customUrl);

        //then
        verify(registryProxy).saveRegistry(RegistryName.BACKGROUND, customUrl);
        verify(uiSystem).notify(ModuleEvent.BACKGROUND_CHANGED, customUrl);
        notificationUtilsStatic.verify(() -> NotificationUtils.success("Wallpaper updated"));
    }

    @Test
    void whenShortCutDetectedThenNoError() {
        SystemBackgroundLayout layout = new SystemBackgroundLayout(registryProxy);
        assertDoesNotThrow(() -> layout.onShortCutDetected(null));
    }

    /**
     * Helper method to bypass Vaadin's component event loop in unit tests
     */
    private void invokeUpdateImage(SystemBackgroundLayout layout, String url) {
        try {
            java.lang.reflect.Method method = SystemBackgroundLayout.class.getDeclaredMethod("updateImage", String.class);
            method.setAccessible(true);
            method.invoke(layout, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
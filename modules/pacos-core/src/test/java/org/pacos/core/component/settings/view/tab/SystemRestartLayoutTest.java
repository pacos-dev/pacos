package org.pacos.core.component.settings.view.tab;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.utils.component.InfoBox;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;

class SystemRestartLayoutTest {

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
    }

    @Test
    void whenGetSearchIndexIsCalledThenConcatenatedConstantStringsAreReturned() {
        //given
        String expectedIndex = "Manually restart the systemRestart system";

        //when
        String actualIndex = SystemRestartLayout.getSearchIndex();

        //then
        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    void whenLayoutIsInitializedThenInfoBoxWithCorrectMessageIsAdded() {
        //given
        SystemRestartLayout layout = new SystemRestartLayout();

        //when
        Optional<Component> infoBox = layout.getChildren()
                .filter(InfoBox.class::isInstance)
                .findFirst();

        //then
        assertTrue(infoBox.isPresent());
        assertEquals("Manually restart the system", ((InfoBox) infoBox.get()).getText());
    }

    @Test
    void whenLayoutIsInitializedThenRestartButtonHasCorrectLabel() {
        //given
        SystemRestartLayout layout = new SystemRestartLayout();

        //when
        Optional<Component> button = layout.getChildren()
                .filter(Button.class::isInstance)
                .findFirst();

        //then
        assertTrue(button.isPresent());
        assertEquals("Restart system", ((Button) button.get()).getText());
    }

    @Test
    void whenLayoutIsInitializedThenRestartButtonIsPrimaryVariant() {
        //given
        SystemRestartLayout layout = new SystemRestartLayout();

        //when
        Button restartButton = (Button) layout.getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElseThrow();

        //then
        assertTrue(restartButton.getThemeNames().contains("primary"));
    }

    @Test
    void whenRestartButtonIsClickedThenConfirmationWindowIsShown() {
        SystemRestartLayout layout = new SystemRestartLayout();
        Button restartButton = (Button) layout.getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElseThrow();

        UISystem uiSystem = mock(UISystem.class);
        WindowManager windowManager = mock(WindowManager.class);
        when(uiSystem.getWindowManager()).thenReturn(windowManager);

        try (var uiSystemMock = mockStatic(UISystem.class)) {
            uiSystemMock.when(UISystem::getCurrent).thenReturn(uiSystem);

            restartButton.click();

            verify(windowManager).showModalWindow(org.mockito.ArgumentMatchers.argThat(config ->
                    config.isWarning() && "Confirmation".equals(config.getTitle())));
        }
    }

}
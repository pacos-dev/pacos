package org.pacos.core.component.settings.view.tab;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class SystemRestartLayoutTest {

    private UISystem uiSystem;

    @BeforeEach
    void init() {
        uiSystem = VaadinMock.mockSystem();
    }

    @Test
    void whenGetSearchIndexIsCalledThenConcatenatedConstantStringsAreReturned() {
        String expectedIndex = "Manually restart the system Restart system";

        String actualIndex = SystemRestartLayout.getSearchIndex();

        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    void whenLayoutIsInitializedThenInfoBoxWithCorrectMessageIsAdded() {
        SystemRestartLayout layout = new SystemRestartLayout();

        Optional<Component> infoBox = layout.getChildren()
                .filter(InfoBox.class::isInstance)
                .findFirst();

        assertTrue(infoBox.isPresent());
        assertEquals("Manually restart the system", ((InfoBox) infoBox.get()).getText());
    }

    @Test
    void whenLayoutIsInitializedThenRestartButtonHasCorrectLabel() {
        SystemRestartLayout layout = new SystemRestartLayout();

        Optional<Component> button = layout.getChildren()
                .filter(Button.class::isInstance)
                .findFirst();

        assertTrue(button.isPresent());
        assertEquals("Restart system", ((Button) button.get()).getText());
    }

    @Test
    void whenLayoutIsInitializedThenRestartButtonIsPrimaryVariant() {
        SystemRestartLayout layout = new SystemRestartLayout();

        Button restartButton = (Button) layout.getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElseThrow();

        assertTrue(restartButton.getThemeNames().contains("primary"));
    }

    @Test
    void whenRestartButtonIsClickedThenConfirmationWindowIsShown() {
        SystemRestartLayout layout = new SystemRestartLayout();
        Button restartButton = (Button) layout.getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElseThrow();

        restartButton.click();

        verify(uiSystem.getWindowManager()).showModalWindow(any(ConfirmationWindowConfig.class));
    }
}
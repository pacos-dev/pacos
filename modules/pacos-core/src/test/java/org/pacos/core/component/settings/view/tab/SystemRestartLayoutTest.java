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
}
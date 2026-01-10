package org.pacos.core.component.settings.view.config;

import com.vaadin.flow.component.Component;
import org.junit.jupiter.api.Test;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.window.shortcut.ShortcutType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionConfigViewTest {

    @Test
    void whenConstructorCalledThenInfoBoxIsAdded() {
        //given

        //when
        PermissionConfigView view = new PermissionConfigView();

        //then
        Optional<Component> infoBox = view.getChildren()
                .filter(InfoBox.class::isInstance)
                .findFirst();

        assertTrue(infoBox.isPresent());
    }

    @Test
    void whenConstructorCalledThenInfoBoxContainsCorrectText() {
        //given
        String expectedSnippet = "Define global authorization policies";

        //when
        PermissionConfigView view = new PermissionConfigView();

        //then
        InfoBox infoBox = (InfoBox) view.getChildren()
                .filter(InfoBox.class::isInstance)
                .findFirst()
                .orElseThrow();

        assertTrue(infoBox.getElement().getText().contains(expectedSnippet));
    }

    @Test
    void whenShortCutDetectedCalledThenNoExceptionIsThrown() {
        //given
        PermissionConfigView view = new PermissionConfigView();

        //when & then
        assertDoesNotThrow(() -> view.onShortCutDetected(ShortcutType.SAVE));
    }
}
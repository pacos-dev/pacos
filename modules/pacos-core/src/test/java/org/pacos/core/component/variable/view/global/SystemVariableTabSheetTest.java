package org.pacos.core.component.variable.view.global;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.event.global.SaveGlobalVariableEvent;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SystemVariableTabSheetTest {

    private GlobalVariableSystem system;
    private SystemVariableTabSheet tabSheet;
    private SystemVariableProxy proxy;

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
        proxy = mock(SystemVariableProxy.class);
        when(proxy.isUnique(org.mockito.ArgumentMatchers.nullable(Integer.class), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);
        system = new GlobalVariableSystem(proxy);
        tabSheet = new SystemVariableTabSheet(system);
    }

    @Test
    void whenCreatedThenNoContentIsShown() {
        assertTrue(tabSheet.getChildren().filter(org.pacos.base.component.NoContent.class::isInstance)
                .findFirst().orElseThrow().isVisible());
        assertFalse(tabSheet.getChildren().filter(TabSheet.class::isInstance)
                .map(TabSheet.class::cast).findFirst().orElseThrow().isVisible());
    }

    @Test
    void whenOpeningGlobalVariableThenTabIsCreatedAndVisible() {
        SystemVariableDTO dto = variable();

        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, dto);

        assertEquals(1, tabSheet.getTabSheet().getChildren().filter(Tab.class::isInstance).count());
        assertTrue(tabSheet.getTabSheet().isVisible());
        assertFalse(tabSheet.getChildren().filter(org.pacos.base.component.NoContent.class::isInstance)
                .findFirst().orElseThrow().isVisible());
    }

    @Test
    void whenOpeningSameGlobalVariableTwiceThenExistingTabIsSelected() {
        SystemVariableDTO dto = variable();

        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, dto);
        Tab selected = tabSheet.getTabSheet().getSelectedTab();
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, dto);

        assertEquals(selected, tabSheet.getTabSheet().getSelectedTab());
        assertEquals(1, tabSheet.getTabSheet().getChildren().filter(Tab.class::isInstance).count());
    }

    @Test
    void whenRemovingOpenGlobalVariableThenTabIsRemoved() {
        SystemVariableDTO dto = variable();
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, dto);

        system.notify(GlobalVariableEvent.REMOVED_ENTRY, dto);

        assertTrue(tabSheet.getTabSheet().getChildren().filter(Tab.class::isInstance).findAny().isEmpty());
        assertFalse(tabSheet.getTabSheet().isVisible());
    }

    @Test
    void whenRemovingUnknownGlobalVariableThenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> system.notify(GlobalVariableEvent.REMOVED_ENTRY, variable()));
    }

    @Test
    void whenRefreshingUnknownGlobalVariableThenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> system.notify(GlobalVariableEvent.REFRESH_ENTRY, variable()));
    }

    @Test
    void whenCloseAllButtonIsClickedThenAllTabsAreRemoved() {
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, variable());
        Button closeAll = tabSheet.getTabSheet().getChildren().filter(Button.class::isInstance)
                .map(Button.class::cast).filter(button -> "Close all".equals(button.getText()))
                .findFirst().orElseThrow();

        closeAll.click();

        assertTrue(tabSheet.getTabSheet().getChildren().filter(Tab.class::isInstance).findAny().isEmpty());
        assertFalse(tabSheet.getTabSheet().isVisible());
    }

    @Test
    void whenCloseButtonIsClickedThenSelectedTabIsRemoved() {
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, variable());
        Tab tab = tabSheet.getTabSheet().getSelectedTab();
        Button close = tab.getChildren().filter(Button.class::isInstance).map(Button.class::cast).findFirst().orElseThrow();

        close.click();

        assertTrue(tabSheet.getTabSheet().getChildren().filter(Tab.class::isInstance).findAny().isEmpty());
        assertFalse(tabSheet.getTabSheet().isVisible());
    }

    @Test
    void whenSavingWithoutSelectedTabThenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> tabSheet.save());
    }

    @Test
    void whenSavingValidVariableThenSaveEventIsFired() {
        SystemVariableDTO dto = variable();
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, dto);

        try (MockedStatic<SaveGlobalVariableEvent> event = mockStatic(SaveGlobalVariableEvent.class)) {
            tabSheet.save();
            event.verify(() -> SaveGlobalVariableEvent.fireEvent(org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.eq(dto), org.mockito.ArgumentMatchers.eq(system)));
        }
    }

    private SystemVariableDTO variable() {
        SystemVariableDTO dto = new SystemVariableDTO();
        dto.setId(1);
        dto.setName("$variable");
        dto.setDescription("Description");
        dto.setJs("return 1;");
        return dto;
    }
}

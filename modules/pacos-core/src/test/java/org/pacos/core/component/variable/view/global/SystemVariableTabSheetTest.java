package org.pacos.core.component.variable.view.global;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.utils.component.TabSheetUtils;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SystemVariableTabSheetTest {

    private GlobalVariableSystem system;
    private SystemVariableProxy proxy;
    private SystemVariableTabSheet layout;
    private TabSheetUtils tabSheet;

    @BeforeEach
    void init() {
        VaadinMock.mockSystem();
        proxy = mock(SystemVariableProxy.class);
        system = new GlobalVariableSystem(proxy);
        layout = new SystemVariableTabSheet(system);
        tabSheet = layout.getChildren()
                .filter(TabSheetUtils.class::isInstance)
                .map(TabSheetUtils.class::cast)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void whenInitializedThenEmptyStateIsVisible() {
        Component noContent = layout.getChildren()
                .filter(component -> !(component instanceof TabSheetUtils))
                .findFirst()
                .orElseThrow();

        assertTrue(noContent.isVisible());
        assertFalse(tabSheet.isVisible());
    }

    @Test
    void whenGlobalVariableTabIsOpenedThenTabSheetBecomesVisible() {
        SystemVariableDTO variable = variable(1, "$environment");

        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, variable);

        assertTrue(tabSheet.isVisible());
        assertNotNull(tabSheet.getSelectedTab());
        assertFalse(layout.getChildren()
                .filter(component -> !(component instanceof TabSheetUtils))
                .findFirst()
                .orElseThrow()
                .isVisible());
    }

    @Test
    void whenSameGlobalVariableTabIsOpenedTwiceThenOnlyOneTabIsCreated() {
        SystemVariableDTO variable = variable(1, "$environment");

        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, variable);
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, variable);

        long tabs = tabSheet.getChildren().filter(TabMonitoring.class::isInstance).count();

        assertTrue(tabSheet.isVisible());
        assertTrue(tabs <= 1);
    }

    @Test
    void whenGlobalVariableEntryIsRemovedThenTabSheetBecomesHidden() {
        SystemVariableDTO variable = variable(1, "$environment");
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, variable);

        system.notify(GlobalVariableEvent.REMOVED_ENTRY, variable);

        assertNull(tabSheet.getSelectedTab());
        assertFalse(tabSheet.isVisible());
    }

    @Test
    void whenSaveIsCalledWithoutSelectedTabThenNothingIsPersisted() {
        layout.save();

        verify(proxy, never()).save(org.mockito.ArgumentMatchers.any(SystemVariableDTO.class));
    }

    @Test
    void whenEntryIsRefreshedThenTabChangesMarkerIsCleared() {
        SystemVariableDTO variable = variable(1, "$environment");
        system.notify(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, variable);
        TabMonitoring tab = (TabMonitoring) tabSheet.getSelectedTab();
        tab.markAsChanged();

        system.notify(GlobalVariableEvent.REFRESH_ENTRY, variable);

        assertFalse(tab.getThemeNames().contains("warning"));
    }

    private static SystemVariableDTO variable(Integer id, String name) {
        SystemVariableDTO variable = new SystemVariableDTO();
        variable.setId(id);
        variable.setName(name);
        return variable;
    }
}
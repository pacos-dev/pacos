package org.pacos.core.component.variable.view.user;

import java.util.List;
import java.util.Set;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pacos.base.event.UISystem;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.event.user.PasteToClipboardSelectedVariables;
import org.pacos.core.component.variable.event.user.PasteVariableEvent;
import org.pacos.core.component.variable.event.user.SaveUserVariableEvent;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.PanelVariable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.pacos.core.component.variable.system.user.UserVariableEvent.OPEN_COLLECTION_VARIABLE_TAB;
import static org.pacos.core.component.variable.system.user.UserVariableEvent.REFRESH_COLLECTION_NAME;

class UserVariableTabSheetTest {

    private UISystem uiSystem;
    private UserVariableSystem variableSystem;
    @Mock
    PanelVariable panel;
    @Mock
    UserVariableCollectionProxy userVariableCollectionProxy;
    @Mock
    UserVariableProxy userVariableProxy;
    private UserVariableTabSheet tabSheet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.uiSystem = VaadinMock.mockSystem();
        this.variableSystem = new UserVariableSystem(panel, uiSystem, userVariableCollectionProxy, userVariableProxy);
        this.tabSheet = new UserVariableTabSheet(variableSystem);
    }

    @Test
    void whenOpenCollectionTabThenTanIsAdded() {
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();
        //when
        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);
        //then
        assertFalse(tabSheet.getSelectedTab().isEmpty());
    }

    @Test
    void whenRefreshVariablesForEmptyContentThenNoException() {
        UserVariableDTO userVariableDTO = new UserVariableDTO();
        //then
        assertDoesNotThrow(() -> uiSystem.notify(UserVariableEvent.REFRESH_VARIABLE, userVariableDTO));
    }

    @Test
    void whenCopyShortcutAndVariableSelectedThenTriggerCopyEvent() {
        //given
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();
        UserVariableDTO item = new UserVariableDTO();
        when(userVariableProxy.loadVariables(collectionDTO.getId())).thenReturn(List.of(item));
        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);

        tabSheet.getSelectedTab().ifPresent(tab -> tab.getVariableGrid().select(item));
        //when
        try (MockedStatic<PasteToClipboardSelectedVariables> mock = Mockito.mockStatic(PasteToClipboardSelectedVariables.class)) {
            variableSystem.notify(UserVariableEvent.COPY_SHORTCUT_EVENT);
            //then
            mock.verify(() -> PasteToClipboardSelectedVariables.fireEvent(Set.of(item)));
        }
    }

    @Test
    void whenCopyShortcutAndVariableNotSelectedThenDoNotTriggerCopyEvent() {
        //given
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();
        UserVariableDTO item = new UserVariableDTO();
        when(userVariableProxy.loadVariables(collectionDTO.getId())).thenReturn(List.of(item));
        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);
        //when
        try (MockedStatic<PasteToClipboardSelectedVariables> mock = Mockito.mockStatic(PasteToClipboardSelectedVariables.class)) {
            variableSystem.notify(UserVariableEvent.COPY_SHORTCUT_EVENT);
            //then
            mock.verifyNoInteractions();
        }
    }

    @Test
    void whenPasteShortcutThenTriggerPasteEvent() {
        //given
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();

        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);
        //when
        try (MockedStatic<PasteVariableEvent> mock = Mockito.mockStatic(PasteVariableEvent.class)) {
            variableSystem.notify(UserVariableEvent.PASTE_SHORTCUT_EVENT);
            //then
            mock.verify(() -> PasteVariableEvent.fireEvent(any(), any()));
        }
    }

    @Test
    void whenSaveShortcutThenTriggerSaveEvent() {
        //given
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();
        UserVariableDTO item = new UserVariableDTO();
        when(userVariableProxy.loadVariables(collectionDTO.getId())).thenReturn(List.of(item));
        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);

        tabSheet.getSelectedTab().ifPresent(tab -> tab.getVariableGrid().select(item));
        //when
        try (MockedStatic<SaveUserVariableEvent> mock = Mockito.mockStatic(SaveUserVariableEvent.class)) {
            variableSystem.notify(UserVariableEvent.SAVE_SHORTCUT_EVENT);
            //then
            mock.verify(() -> SaveUserVariableEvent.fireEvent(any(), any(), any()));
        }
    }

    @Test
    void whenCloseTabThenTabIsRemoved() {
        //given
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();
        UserVariableDTO item = new UserVariableDTO();
        when(userVariableProxy.loadVariables(collectionDTO.getId())).thenReturn(List.of(item));
        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);

        tabSheet.getSelectedTab().ifPresent(tab -> tab.getVariableGrid().select(item));
        //when
        assertFalse(tabSheet.getSelectedTab().isEmpty());
        variableSystem.notify(UserVariableEvent.CLOSE_TAB, collectionDTO);
        //then
        assertTrue(tabSheet.getSelectedTab().isEmpty());
    }

    @Test
    void whenCloseAllThenTabIsRemoved() {
        //given
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();
        UserVariableDTO item = new UserVariableDTO();
        when(userVariableProxy.loadVariables(collectionDTO.getId())).thenReturn(List.of(item));
        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);

        tabSheet.getSelectedTab().ifPresent(tab -> tab.getVariableGrid().select(item));
        //when
        assertFalse(tabSheet.getSelectedTab().isEmpty());
        tabSheet.closeAll();
        //then
        assertTrue(tabSheet.getSelectedTab().isEmpty());
    }

    @Test
    void whenDeleteShortcutThenRemoveVariable() {
        //given
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();
        UserVariableDTO item = new UserVariableDTO();
        when(userVariableProxy.loadVariables(collectionDTO.getId())).thenReturn(List.of(item));
        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);

        tabSheet.getSelectedTab().ifPresent(tab -> tab.getVariableGrid().select(item));
        //when

        variableSystem.notify(UserVariableEvent.DELETE_SHORTCUT_EVENT);
        //then
        assertTrue(tabSheet.getSelectedTab().isPresent());
        assertTrue(tabSheet.getSelectedTab().get().getVariableGrid().getVariables().isEmpty());
    }

    @Test
    void whenRefreshTabNameThenUpdateLabel() {
        //given
        UserVariableCollectionDTO collectionDTO = UserVariableCollectionDTO.builder().build();
        UserVariableDTO item = new UserVariableDTO();
        item.setName("test");
        when(userVariableProxy.loadVariables(collectionDTO.getId())).thenReturn(List.of(item));
        variableSystem.notify(OPEN_COLLECTION_VARIABLE_TAB, collectionDTO);
        //when
        collectionDTO.setName("test2");
        variableSystem.notify(REFRESH_COLLECTION_NAME, collectionDTO);
        //when
        //then
        assertEquals("test2",tabSheet.getTabSheet().getTabAt(0).getLabel());
    }
}
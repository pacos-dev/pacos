package org.pacos.core.component.variable.view.plugin;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.event.user.PasteToClipboardSelectedVariables;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class VariablePluginTest {

    private UserVariableCollectionProxy collectionProxy;
    private UserVariableProxy variableProxy;
    private UserProxyService userProxyService;
    private VariablePlugin variablePlugin;

    @BeforeEach
    void setUp() {
        //given
        VaadinMock.mockSystem();
        collectionProxy = mock(UserVariableCollectionProxy.class);
        variableProxy = mock(UserVariableProxy.class);
        userProxyService = mock(UserProxyService.class);

        UserVariableCollectionDTO global = mock(UserVariableCollectionDTO.class);
        when(global.isGlobal()).thenReturn(true);
        when(global.getId()).thenReturn(100);

        when(collectionProxy.loadUserCollections(anyInt())).thenReturn(List.of(global));
        when(variableProxy.loadVariables(100)).thenReturn(new ArrayList<>());
        this.variablePlugin = new VariablePlugin(collectionProxy, variableProxy, userProxyService);
    }

    @Test
    void whenVariablePluginIsClosedThenDoNotTriggerCopyFunction() {
        //when
        try (MockedStatic<PasteToClipboardSelectedVariables> mock = mockStatic(PasteToClipboardSelectedVariables.class)) {
            //when
            variablePlugin.copyVariablesToClipboard();
            //then
            mock.verifyNoInteractions();
        }
    }

    @Test
    void whenCopyShortcutOnGlobalThenPasteSelectedVariableToSystemClipboard() {
        variablePlugin.open();
        variablePlugin.globalGrid.getElement().setAttribute("focused", "focused");
        UserVariableDTO variable = new UserVariableDTO();
        variablePlugin.globalGrid.setVariables(List.of(variable));
        variablePlugin.globalGrid.select(variable);
        //when
        try (MockedStatic<PasteToClipboardSelectedVariables> mock = mockStatic(PasteToClipboardSelectedVariables.class)) {
            //when
            variablePlugin.copyVariablesToClipboard();
            //then
            mock.verify(() -> PasteToClipboardSelectedVariables.fireEvent(Set.of(variable)));
        }
    }

    @Test
    void whenCopyShortcutOnNormalGridThenPasteSelectedVariableToSystemClipboard() {
        variablePlugin.open();
        variablePlugin.variableGrid.getElement().setAttribute("focused", "focused");
        UserVariableDTO variable = new UserVariableDTO();
        variablePlugin.variableGrid.setVariables(List.of(variable));
        variablePlugin.variableGrid.select(variable);
        //when
        try (MockedStatic<PasteToClipboardSelectedVariables> mock = mockStatic(PasteToClipboardSelectedVariables.class)) {
            //when
            variablePlugin.copyVariablesToClipboard();
            //then
            mock.verify(() -> PasteToClipboardSelectedVariables.fireEvent(Set.of(variable)));
        }
    }

    @Test
    void whenCopyShortcutButNotFocusedGridThenDoNothing() {
        variablePlugin.open();
        //when
        try (MockedStatic<PasteToClipboardSelectedVariables> mock = mockStatic(PasteToClipboardSelectedVariables.class)) {
            //when
            variablePlugin.copyVariablesToClipboard();
            //then
            mock.verifyNoInteractions();
        }
    }

    @Test
    void whenCollectionSetChangedByUserThenRefreshCollectionSelect(){
        //given
        UserVariableCollectionDTO collectionDTO = mock(UserVariableCollectionDTO.class);
        UserVariableDTO variableDTO = new UserVariableDTO();
        variableDTO.setName("TEST");
        variableDTO.setCurrentValue("test");
        var variableList = List.of(variableDTO);
        when(collectionDTO.getId()).thenReturn(0);
        when(collectionDTO.getVariables()).thenReturn(variableList);
        when(collectionDTO.getDisplayName()).thenReturn("test");
        when(collectionProxy.loadUserCollections(UserSession.getCurrent().getUserId())).thenReturn(List.of(collectionDTO));
        when(variableProxy.loadVariables(0)).thenReturn(variableList);
        //when
        UISystem.getCurrent().notify(UserVariableEvent.REFRESH_COLLECTION_LIST);
        //then
        assertEquals(variablePlugin.variableGrid.getVariables(), variableList);
    }

    @Test
    void whenCollectionSetChangedByUserAndCollectionIsNotSelectedThenGridIsEmpty(){
        //given
        UserSession.getCurrent().getUser().setVariableCollectionId(null);
        UserVariableCollectionDTO collectionDTO = mock(UserVariableCollectionDTO.class);

        when(collectionDTO.getId()).thenReturn(0);
        when(collectionDTO.getDisplayName()).thenReturn("test");
        when(collectionProxy.loadUserCollections(UserSession.getCurrent().getUserId())).thenReturn(List.of(collectionDTO));
        //when
        UISystem.getCurrent().notify(UserVariableEvent.REFRESH_COLLECTION_LIST);
        //then
        assertTrue(variablePlugin.variableGrid.getVariables().isEmpty());
    }

}
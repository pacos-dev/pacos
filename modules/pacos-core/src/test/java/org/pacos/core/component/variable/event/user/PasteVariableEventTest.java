package org.pacos.core.component.variable.event.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pacos.base.event.UISystem;
import org.pacos.base.window.manager.ClipboardManager;
import org.pacos.common.view.param.Param;
import org.pacos.common.view.param.ParamParser;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.user.UserVariableForm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasteVariableEventTest {

    @Test
    void whenTriggeredThenNoExceptions() {
        UserVariableSystem system = Mockito.mock(UserVariableSystem.class);
        when(system.getUiSystem()).thenReturn(mock(UISystem.class));
        when(system.getUiSystem().getClipboardManager()).thenReturn(Mockito.mock(ClipboardManager.class));
        when(system.getUiSystem().getClipboardManager().readClipboard()).thenReturn(mock(CompletableFuture.class));
        UserVariableForm form = Mockito.mock(UserVariableForm.class);
        UI mockUI = Mockito.mock(UI.class);
        UI.setCurrent(mockUI);
        //when
        assertDoesNotThrow(() -> PasteVariableEvent.fireEvent(system, form));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"asdcxzc", "czxcsafsa:Adsada"})
    void whenClipboardValueIncorrectThenNoExceptions(String value) {
        UserVariableForm form = Mockito.mock(UserVariableForm.class);
        UI mockUI = Mockito.mock(UI.class);
        when(form.getVariables()).thenReturn(new ArrayList<>());
        CompletableFuture<String> valueFeature = CompletableFuture.completedFuture(value);
        //then
        assertDoesNotThrow(() -> PasteVariableEvent.readClipboardAndModifyVariables(form, valueFeature, mockUI));
    }

    @Test
    void whenPasteVariableThenExtendsResultList() {
        UserVariableForm form = Mockito.mock(UserVariableForm.class);
        UI mockUI = Mockito.mock(UI.class);
        when(form.getVariables()).thenReturn(List.of());
        String variableString = ParamParser.mapToString(List.of(new Param("name", "value", true)));
        CompletableFuture<String> valueFeature = CompletableFuture.completedFuture(variableString);
        //when
        PasteVariableEvent.readClipboardAndModifyVariables(form, valueFeature, mockUI);
        //when
        ArgumentCaptor<Command> commandArgumentCaptor = ArgumentCaptor.forClass(Command.class);
        verify(mockUI).access(commandArgumentCaptor.capture());
        commandArgumentCaptor.getValue().execute();
        //then
        ArgumentCaptor<List<UserVariableDTO>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(form).modifyVariables(listCaptor.capture());
        assertEquals(1, listCaptor.getValue().size());
        assertEquals("name", listCaptor.getValue().iterator().next().getName());
        assertEquals("value", listCaptor.getValue().iterator().next().getCurrentValue());
    }

    @Test
    void whenNoResponseFromClientThenNoExceptions() {
        UserVariableForm form = Mockito.mock(UserVariableForm.class);
        UI mockUI = Mockito.mock(UI.class);
        when(form.getVariables()).thenReturn(List.of());
        CompletableFuture<String> valueFeature = CompletableFuture.failedFuture(new TimeoutException());
        //when
        assertDoesNotThrow(() -> PasteVariableEvent.readClipboardAndModifyVariables(form, valueFeature, mockUI));
    }

    @Test
    void whenPastedParamMatchesUserVariableThenUpdateCurrentValueAndEnable() {
        List<UserVariableDTO> userVariables = new ArrayList<>();
        UserVariableDTO existingVariable = new UserVariableDTO();
        existingVariable.setName("var1");
        existingVariable.setCurrentValue("oldValue");
        existingVariable.setEnabled(false);
        userVariables.add(existingVariable);

        Set<Param> pastedParam = new HashSet<>();
        Param param = new Param("var1", "newValue", true);
        pastedParam.add(param);

        List<UserVariableDTO> result = PasteVariableEvent.joinVariables(userVariables, pastedParam);

        assertEquals(1, result.size());
        UserVariableDTO updatedVariable = result.get(0);
        assertEquals("var1", updatedVariable.getName());
        assertEquals("newValue", updatedVariable.getCurrentValue());
        assertTrue(updatedVariable.isEnabled());
    }

    @Test
    void whenPastedParamDoesNotMatchThenAddNewUserVariable() {
        List<UserVariableDTO> userVariables = new ArrayList<>();

        Set<Param> pastedParam = new HashSet<>();
        Param param = new Param("var1", "value1", true);
        pastedParam.add(param);

        List<UserVariableDTO> result = PasteVariableEvent.joinVariables(userVariables, pastedParam);

        assertEquals(1, result.size());
        UserVariableDTO newVariable = result.get(0);
        assertEquals("var1", newVariable.getName());
        assertEquals("value1", newVariable.getCurrentValue());
        assertEquals("value1", newVariable.getInitialValue());
    }

    @Test
    void whenMultipleParamsAddedOrUpdatedThenHandleAllCorrectly() {
        List<UserVariableDTO> userVariables = new ArrayList<>();
        UserVariableDTO existingVariable = new UserVariableDTO();
        existingVariable.setName("var1");
        existingVariable.setCurrentValue("oldValue");
        userVariables.add(existingVariable);

        Set<Param> pastedParam = new HashSet<>();
        pastedParam.add(new Param("var1", "newValue", true));
        pastedParam.add(new Param("var2", "value2", true));

        List<UserVariableDTO> result = PasteVariableEvent.joinVariables(userVariables, pastedParam);

        assertEquals(2, result.size());

        UserVariableDTO updatedVariable = result.stream().filter(v -> "var1".equals(v.getName())).findFirst().orElseThrow();
        assertEquals("newValue", updatedVariable.getCurrentValue());
        assertTrue(updatedVariable.isEnabled());

        UserVariableDTO newVariable = result.stream().filter(v -> "var2".equals(v.getName())).findFirst().orElseThrow();
        assertEquals("value2", newVariable.getCurrentValue());
        assertEquals("value2", newVariable.getInitialValue());
    }
}
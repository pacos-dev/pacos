package org.pacos.core.component.variable.view.global;

import com.vaadin.flow.component.formlayout.FormLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VariableFormTest {

    private SystemVariableDTO dto;
    private VariableForm form;

    @BeforeEach
    void setUp() {
        dto = new SystemVariableDTO();
        dto.setId(7);
        dto.setName("$original");
        dto.setDescription("Original description");
        dto.setJs("return 1;");

        SystemVariableProxy proxy = mock(SystemVariableProxy.class);
        when(proxy.isUnique(nullable(Integer.class), anyString())).thenReturn(true);
        GlobalVariableSystem system = mock(GlobalVariableSystem.class);
        when(system.getSystemVariableProxy()).thenReturn(proxy);

        form = new VariableForm(system, dto, mock(TabMonitoring.class));
    }

    @Test
    void whenCreatedWithValidVariableThenFormIsValid() {
        assertTrue(form.isValid());
    }

    @Test
    void whenNameDoesNotStartWithDollarThenFormIsInvalid() {
        nameField().setValue("invalid");

        assertFalse(form.isValid());
    }

    @Test
    void whenValidValuesAreChangedThenGetBeanUpdatesOriginalDto() {
        nameField().setValue("$updated");
        descriptionField().setValue("Updated description");

        SystemVariableDTO result = form.getBean();

        assertSame(dto, result);
        assertEquals("$updated", result.getName());
        assertEquals("Updated description", result.getDescription());
    }

    private TextFieldUtils nameField() {
        FormLayout fields = (FormLayout) form.getChildren().findFirst().orElseThrow();
        return (TextFieldUtils) fields.getChildren().findFirst().orElseThrow();
    }

    private TextFieldUtils descriptionField() {
        FormLayout fields = (FormLayout) form.getChildren().findFirst().orElseThrow();
        return (TextFieldUtils) fields.getChildren().skip(1).findFirst().orElseThrow();
    }
}

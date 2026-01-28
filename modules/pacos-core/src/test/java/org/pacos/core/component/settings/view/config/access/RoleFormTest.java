package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.security.dto.RoleDTO;

import com.vaadin.flow.data.binder.ValidationException;

class RoleFormTest {

    private RoleForm roleForm;
    private RoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        roleDTO = new RoleDTO();
        roleDTO.setLabel("Initial Label");
        roleDTO.setDescription("Initial Description");
        roleForm = new RoleForm(roleDTO);
    }

    @Test
    void whenLabelIsValidThenValidateReturnsTrue() {
        //given
        roleDTO.setLabel("Valid Name");

        //when
        boolean isValid = roleForm.validate();

        //then
        assertTrue(isValid);
    }

    @Test
    void whenGetBeanThenReturnRoleDTO() throws ValidationException {
        //when
        RoleDTO role = roleForm.getBean();

        //then
        assertNotNull(role);
    }
}
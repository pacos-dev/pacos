package org.pacos.core.component.settings.view.config.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.core.component.security.dto.RoleDTO;

class RolePermissionWindowConfigTest {

    private RolePermissionWindowConfig config;
    private RoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        roleDTO = new RoleDTO();
        roleDTO.setId(1);
        roleDTO.setLabel("ADMIN");
        config = new RolePermissionWindowConfig(roleDTO);
    }

    @Test
    void whenTitleCalledThenReturnTitleWithRoleLabel() {
        //when
        String result = config.title();

        //then
        assertEquals("Permissions for role ADMIN", result);
    }

    @Test
    void whenIconCalledThenReturnUnlockIconUrl() {
        //when
        String result = config.icon();

        //then
        assertEquals(PacosIcon.UNLOCK.getUrl(), result);
    }

    @Test
    void whenActivatorClassCalledThenReturnPanelRolePermissionWindow() {
        //when
        Class<?> result = config.activatorClass();

        //then
        assertEquals(PanelRolePermissionWindow.class, result);
    }

    @Test
    void whenIsApplicationCalledThenReturnFalse() {
        //when
        boolean result = config.isApplication();

        //then
        assertFalse(result);
    }

    @Test
    void whenIsAllowMultipleInstanceCalledThenReturnTrue() {
        //when
        boolean result = config.isAllowMultipleInstance();

        //then
        assertTrue(result);
    }

    @Test
    void whenEqualsCalledWithSameRoleDtoThenReturnTrue() {
        //given
        RolePermissionWindowConfig otherConfig = new RolePermissionWindowConfig(roleDTO);

        //when
        boolean result = config.equals(otherConfig);

        //then
        assertTrue(result);
    }

    @Test
    void whenEqualsCalledWithDifferentRoleDtoThenReturnFalse() {
        //given
        RoleDTO otherRole = new RoleDTO();
        otherRole.setId(2);
        RolePermissionWindowConfig otherConfig = new RolePermissionWindowConfig(otherRole);

        //when
        boolean result = config.equals(otherConfig);

        //then
        assertFalse(result);
    }

    @Test
    void whenHashCodeCalledThenReturnHashCodeBasedOnRoleDto() {
        //given
        RolePermissionWindowConfig otherConfig = new RolePermissionWindowConfig(roleDTO);

        //when
        int hashCode1 = config.hashCode();
        int hashCode2 = otherConfig.hashCode();

        //then
        assertEquals(hashCode1, hashCode2);
    }
}
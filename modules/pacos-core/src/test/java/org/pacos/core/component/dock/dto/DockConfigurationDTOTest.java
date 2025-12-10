package org.pacos.core.component.dock.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DockConfigurationDTOTest {

    @Test
    void whenCreatedWithBuilderThenFieldsAreSetCorrectly() {
        DockConfigurationDTO dockDTO = new DockConfigurationDTO(1);
        dockDTO.setUserId(123);
        dockDTO.setActivator("com.example.Activator");
        dockDTO.setOrderNum(42);

        assertEquals(1, dockDTO.getId());
        assertEquals(123, dockDTO.getUserId());
        assertEquals("com.example.Activator", dockDTO.getActivator());
        assertEquals(42, dockDTO.getOrderNum());
    }

    @Test
    void whenSetUserIdThenFieldIsUpdated() {
        DockConfigurationDTO dockDTO = new DockConfigurationDTO(1);
        dockDTO.setUserId(321);

        assertEquals(321, dockDTO.getUserId());
    }

    @Test
    void whenSetActivatorThenFieldIsUpdated() {
        DockConfigurationDTO dockDTO = new DockConfigurationDTO(1);
        dockDTO.setActivator("com.example.NewActivator");

        assertEquals("com.example.NewActivator", dockDTO.getActivator());
    }

    @Test
    void whenSetOrderNumThenFieldIsUpdated() {
        DockConfigurationDTO dockDTO = new DockConfigurationDTO(1);
        dockDTO.setOrderNum(99);

        assertEquals(99, dockDTO.getOrderNum());
    }

    @Test
    void whenEqualsAndHashCodeThenBasedOnId() {
        DockConfigurationDTO dockDTO1 = new DockConfigurationDTO(1);
        DockConfigurationDTO dockDTO2 = new DockConfigurationDTO(1);

        assertEquals(dockDTO1, dockDTO2);
        assertEquals(dockDTO1.hashCode(), dockDTO2.hashCode());
    }

    @Test
    void whenDifferentIdThenNotEqual() {
        DockConfigurationDTO dockDTO1 = new DockConfigurationDTO(1);
        DockConfigurationDTO dockDTO2 = new DockConfigurationDTO(2);

        assertNotEquals(dockDTO1, dockDTO2);
    }
}

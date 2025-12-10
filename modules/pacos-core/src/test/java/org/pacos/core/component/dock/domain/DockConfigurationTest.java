package org.pacos.core.component.dock.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DockConfigurationTest {

    @Test
    void whenCreatedWithNoArgsConstructorThenFieldsAreNullOrDefault() {
        DockConfiguration dockConfiguration = new DockConfiguration();
        assertNull(dockConfiguration.getId());
        assertEquals(0, dockConfiguration.getOrderNum());
        assertNull(dockConfiguration.getActivatorClass());
        assertNull(dockConfiguration.getUserId());
    }

    @Test
    void whenCreatedWithAllArgsConstructorThenFieldsAreSetCorrectly() {
        DockConfiguration dockConfiguration = new DockConfiguration("com.example.Activator", 123);
        assertEquals("com.example.Activator", dockConfiguration.getActivatorClass());
        assertEquals(123, dockConfiguration.getUserId());
    }

    @Test
    void whenSetOrderNumThenFieldIsUpdated() {
        DockConfiguration dockConfiguration = new DockConfiguration();
        dockConfiguration.setOrderNum(42);
        assertEquals(42, dockConfiguration.getOrderNum());
    }

    @Test
    void whenSetActivatorClassThenFieldIsUpdated() {
        DockConfiguration dockConfiguration = new DockConfiguration();
        dockConfiguration.setActivatorClass("com.example.NewActivator");
        assertEquals("com.example.NewActivator", dockConfiguration.getActivatorClass());
    }

    @Test
    void whenSetUserIdThenFieldIsUpdated() {
        DockConfiguration dockConfiguration = new DockConfiguration();
        dockConfiguration.setUserId(321);
        assertEquals(321, dockConfiguration.getUserId());
    }

    @Test
    void whenEqualsAndHashCodeThenBasedOnId() {
        DockConfiguration dock1 = new DockConfiguration();
        DockConfiguration dock2 = new DockConfiguration();
        dock1.setId(1);
        dock2.setId(1);
        assertEquals(dock1, dock2);
        assertEquals(dock1.hashCode(), dock2.hashCode());
    }

    @Test
    void whenDifferentIdThenNotEqual() {
        DockConfiguration dock1 = new DockConfiguration();
        DockConfiguration dock2 = new DockConfiguration();
        dock1.setId(1);
        dock2.setId(2);
        assertNotEquals(dock1, dock2);
    }

    @Test
    void whenDifferentIdThenHashCodeNotEqual() {
        DockConfiguration dock1 = new DockConfiguration();
        DockConfiguration dock2 = new DockConfiguration();
        dock1.setId(1);
        dock2.setId(2);
        assertNotEquals(dock1.hashCode(), dock2.hashCode());
    }

}

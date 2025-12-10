package org.pacos.core.component.registry.proxy;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.registry.service.RegistryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistryProxyTest {

    private final RegistryService registryService = Mockito.mock(RegistryService.class);
    private final RegistryProxy registryProxy = new RegistryProxy(registryService);

    @Test
    void whenSaveRegistryWithObjectThenDelegatesToService() {
        RegistryName registryName = RegistryName.INSTALLED;
        Object value = new Object();
        registryProxy.saveRegistry(registryName, value);
        verify(registryService).saveRegistry(registryName, value);
    }

    @Test
    void whenSaveRegistryWithStringThenDelegatesToService() {
        RegistryName registryName = RegistryName.INSTALLED;
        String value = "test";
        registryProxy.saveRegistry(registryName, value);
        verify(registryService).saveRegistry(registryName, value);
    }

    @Test
    void whenReadRegistryWithClassThenDelegatesToService() {
        RegistryName registryName = RegistryName.INSTALLED;
        Class<String> clazz = String.class;
        Optional<String> expected = Optional.of("test");
        when(registryService.readRegistry(registryName, clazz)).thenReturn(expected);
        Optional<String> result = registryProxy.readRegistry(registryName, clazz);
        assertEquals(expected, result);
    }

    @Test
    void whenReadRegistryThenDelegatesToService() {
        RegistryName registryName = RegistryName.INSTALLED;
        Optional<String> expected = Optional.of("test");
        when(registryService.readRegistry(registryName)).thenReturn(expected);
        Optional<String> result = registryProxy.readRegistry(registryName);
        assertEquals(expected, result);
    }

    @Test
    void whenReadRegistryOrDefaultThenReturnsDefaultIfEmpty() {
        RegistryName registryName = RegistryName.INSTALLED;
        String defaultValue = "default";
        when(registryService.readRegistry(registryName)).thenReturn(Optional.empty());
        String result = registryProxy.readRegistryOrDefault(registryName, defaultValue);
        assertEquals(defaultValue, result);
    }

    @Test
    void whenReadBooleanThenReturnsParsedValue() {
        RegistryName registryName = RegistryName.INSTALLED;
        when(registryService.readRegistry(registryName)).thenReturn(Optional.of("true"));
        Boolean result = registryProxy.readBoolean(registryName, false);
        assertTrue(result);
    }

    @Test
    void whenReadBooleanThenReturnsDefaultIfEmpty() {
        RegistryName registryName = RegistryName.INSTALLED;
        Boolean defaultValue = false;
        when(registryService.readRegistry(registryName)).thenReturn(Optional.empty());
        Boolean result = registryProxy.readBoolean(registryName, defaultValue);
        assertEquals(defaultValue, result);
    }

    @Test
    void whenDeleteThenDelegatesToService() {
        RegistryName registryName = RegistryName.INSTALLED;
        registryProxy.delete(registryName);
        verify(registryService).delete(registryName);
    }

    @Test
    void whenIsSystemToUpdateThenReturnsTrueIfVersionsDiffer() {
        when(registryService.readRegistry(RegistryName.SYSTEM_VERSION)).thenReturn(Optional.of("1.0"));
        when(registryService.readRegistry(RegistryName.AVAILABLE_SYSTEM_VERSION)).thenReturn(Optional.of("2.0"));
        assertTrue(registryProxy.isSystemToUpdate());
    }

    @Test
    void whenIsSystemToUpdateThenReturnsFalseIfVersionsAreEqual() {
        when(registryService.readRegistry(RegistryName.SYSTEM_VERSION)).thenReturn(Optional.of("1.0"));
        when(registryService.readRegistry(RegistryName.AVAILABLE_SYSTEM_VERSION)).thenReturn(Optional.of("1.0"));
        assertFalse(registryProxy.isSystemToUpdate());
    }

    @Test
    void whenIsInstalledThenReturnsCorrectValue() {
        when(registryService.readRegistry(RegistryName.INSTALLED)).thenReturn(Optional.of("true"));
        assertTrue(registryProxy.isInstalled());
    }

    @Test
    void whenIsSingleModeThenReturnsCorrectValue() {
        when(registryService.readRegistry(RegistryName.SINGLE_MODE)).thenReturn(Optional.of("true"));
        assertTrue(registryProxy.isSingleMode());
    }

    @Test
    void whenIsGuestModeThenReturnsCorrectValue() {
        when(registryService.readRegistry(RegistryName.GUEST_MODE)).thenReturn(Optional.of("true"));
        assertTrue(registryProxy.isGuestMode());
    }

    @Test
    void whenIsRegistrationPanelEnabledThenReturnsCorrectValue() {
        when(registryService.readRegistry(RegistryName.REGISTRATION_PANEL)).thenReturn(Optional.of("true"));
        assertTrue(registryProxy.isRegistrationPanelEnabled());
    }
}

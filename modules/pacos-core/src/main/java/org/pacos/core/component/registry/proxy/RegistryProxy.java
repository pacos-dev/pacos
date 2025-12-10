package org.pacos.core.component.registry.proxy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.pacos.base.utils.TimeToStringUtils;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.registry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistryProxy {
    private final RegistryService registryService;

    @Autowired
    public RegistryProxy(RegistryService registryService) {
        this.registryService = registryService;
    }

    public void saveRegistry(RegistryName registryName, Object value) {
        registryService.saveRegistry(registryName, value);
    }

    public void saveRegistry(RegistryName registryName, String value) {
        registryService.saveRegistry(registryName, value);
    }

    public <T> Optional<T> readRegistry(RegistryName registryName, Class<T> clazz) {
        return registryService.readRegistry(registryName, clazz);
    }

    public <T> T readRegistry(RegistryName registryName, Class<T> clazz, T defaultValue) {
        return registryService.readRegistry(registryName, clazz).orElse(defaultValue);
    }

    public Optional<String> readRegistry(RegistryName registryName) {
        return registryService.readRegistry(registryName);
    }

    public String readRegistryOrDefault(RegistryName registryName, String defaultValue) {
        return registryService.readRegistry(registryName).orElse(defaultValue);
    }

    public boolean isInstalled() {
        return readBoolean(RegistryName.INSTALLED, false);
    }

    public Boolean isSingleMode() {
        return readBoolean(RegistryName.SINGLE_MODE, false);
    }

    public boolean isGuestMode() {
        return readBoolean(RegistryName.GUEST_MODE, false);
    }

    public boolean isRegistrationPanelEnabled() {
        return readBoolean(RegistryName.REGISTRATION_PANEL, false);
    }

    public Boolean readBoolean(RegistryName registryName, Boolean defaultValue) {
        Optional<String> value = registryService.readRegistry(registryName);
        return value.map(Boolean::valueOf).orElse(defaultValue);
    }

    public void delete(RegistryName registryName) {
        registryService.delete(registryName);
    }

    public boolean isSystemToUpdate() {
        String currentVersion = readRegistryOrDefault(RegistryName.SYSTEM_VERSION, "1.0");
        String availableVersion = readRegistry(RegistryName.AVAILABLE_SYSTEM_VERSION).orElse(null);
        return currentVersion != null && availableVersion != null && !currentVersion.equals(availableVersion);
    }

    public boolean isPluginToUpdate() {
        return readRegistry(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, Integer.class, 0) > 0;
    }


    public boolean isRestartRequired() {
        return readBoolean(RegistryName.RESTART_REQUIRED, false);
    }

    public String readTime(RegistryName registryName, String defaultValue) {
        long valueInMs = readRegistry(registryName, Long.class, 0L);
        return valueInMs == 0L ? defaultValue : TimeToStringUtils.format(
                LocalDateTime.ofInstant(Instant.ofEpochMilli(valueInMs), ZoneId.systemDefault()));
    }
}

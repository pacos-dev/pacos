package org.pacos.core.component.plugin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.pacos.base.exception.PacosException;
import org.pacos.config.property.WorkingDir;
import org.pacos.core.component.plugin.domain.AppPlugin;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.repository.PacosPluginRepository;

class PluginServiceTest {

    private PacosPluginRepository repository;
    private PluginService pluginService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repository = mock(PacosPluginRepository.class);
        pluginService = new PluginService(repository);
    }

    @Test
    void disablePluginMarksDtoAndEntityDisabled() {
        PluginDTO dto = plugin("1.0");
        AppPlugin entity = new AppPlugin("com.example", "plugin", "1.0");
        when(repository.findByGroupIdAndArtifactNameAndDisabled("com.example", "plugin", false))
                .thenReturn(Optional.of(entity));

        pluginService.disablePlugin(dto);

        assertTrue(dto.isDisabled());
        assertTrue(entity.isDisabled());
        verify(repository).save(entity);
    }

    @Test
    void enablePluginMarksDtoAndEntityEnabled() {
        PluginDTO dto = plugin("1.0");
        dto.setDisabled(true);
        AppPlugin entity = new AppPlugin("com.example", "plugin", "1.0");
        entity.setDisabled(true);
        when(repository.findByGroupIdAndArtifactNameAndDisabled("com.example", "plugin", true))
                .thenReturn(Optional.of(entity));

        pluginService.enablePlugin(dto);

        assertFalse(dto.isDisabled());
        assertFalse(entity.isDisabled());
        verify(repository).save(entity);
    }

    @Test
    void enableOrDisableMissingPluginThrowsPacosException() {
        PluginDTO dto = plugin("1.0");
        when(repository.findByGroupIdAndArtifactNameAndDisabled("com.example", "plugin", true))
                .thenReturn(Optional.empty());

        assertThrows(PacosException.class, () -> pluginService.enablePlugin(dto));
    }

    @Test
    void removePluginVersionDeletesOnlyMatchingVersionAndItsFile() {
        PluginDTO dto = plugin("1.0");
        AppPlugin matching = new AppPlugin("com.example", "plugin", "1.0");
        AppPlugin otherVersion = new AppPlugin("com.example", "plugin", "2.0");
        when(repository.findByArtifactNameAndGroupId("plugin", "com.example"))
                .thenReturn(List.of(matching, otherVersion));

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(tempDir);
            pluginService.removePluginVersion(dto);
        }

        verify(repository).deleteAll(List.of(matching));
    }

    @Test
    void removePluginDeletesAllVersionsAndFiles() {
        PluginDTO dto = plugin("1.0");
        AppPlugin v1 = new AppPlugin("com.example", "plugin", "1.0");
        AppPlugin v2 = new AppPlugin("com.example", "plugin", "2.0");
        when(repository.findByArtifactNameAndGroupId("plugin", "com.example")).thenReturn(List.of(v1, v2));

        try (MockedStatic<WorkingDir> workingDir = mockStatic(WorkingDir.class)) {
            workingDir.when(WorkingDir::getLibPath).thenReturn(tempDir);
            pluginService.removePlugin(dto);
        }

        verify(repository).deleteAll(List.of(v1, v2));
    }

    @Test
    void findMethodsMapRepositoryEntitiesToDtos() {
        AppPlugin entity = new AppPlugin("com.example", "plugin", "1.0");
        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.findAllByDisabled(false)).thenReturn(List.of(entity));
        when(repository.findAllByRemoved(false)).thenReturn(List.of(entity));
        when(repository.findByArtifactNameAndGroupId("plugin", "com.example")).thenReturn(List.of(entity));

        assertEquals(1, pluginService.findAll().size());
        assertEquals(1, pluginService.findEnabledPlugin().size());
        assertEquals(1, pluginService.findNotRemovedPlugin().size());
        assertEquals(1, pluginService.findByArtifactNameAndGroupId("plugin", "com.example").size());
    }

    private static PluginDTO plugin(String version) {
        PluginDTO dto = new PluginDTO();
        dto.setGroupId("com.example");
        dto.setArtifactName("plugin");
        dto.setVersion(version);
        return dto;
    }
}

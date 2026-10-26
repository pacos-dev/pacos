package org.pacos.core.component.repository.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.repository.domain.PluginRepository;
import org.pacos.core.component.repository.repository.PluginRepositoryRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PluginRepositoryServiceTest {

    private PluginRepositoryRepository repository;
    private RegistryProxy registryProxy;
    private PluginRepositoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(PluginRepositoryRepository.class);
        registryProxy = mock(RegistryProxy.class);
        service = new PluginRepositoryService(repository, registryProxy);
    }

    @Test
    void whenNormalizeRepositoryUrlThenTrimAndRemoveTrailingSlashes() {
        assertEquals("https://repo.example.com/repository/plugins",
                PluginRepositoryService.normalizeUrl("  https://repo.example.com/repository/plugins///  "));
    }


    @Test
    void whenNormalizeRepositoryUrlIsBlankThenRejectUrl() {
        assertThrows(IllegalArgumentException.class, () -> PluginRepositoryService.normalizeUrl("  "));
    }

    @Test
    void whenNormalizeRepositoryUrlExceedsMaximumLengthThenRejectUrl() {
        String url = "https://repo.example.com/" + "a".repeat(2048);

        assertThrows(IllegalArgumentException.class, () -> PluginRepositoryService.normalizeUrl(url));
    }

    @Test
    void whenAddRepositoryWithNameExceedingMaximumLengthThenRejectRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addRepository("R".repeat(121), "https://repo.example.com/maven"));

        verifyNoInteractions(repository, registryProxy);
    }

    @Test
    void whenSelectRepositoryWithNullIdThenRejectSelection() {
        assertThrows(IllegalArgumentException.class, () -> service.selectRepository(null));

        verifyNoInteractions(repository, registryProxy);
    }

    @Test
    void whenDeleteRepositoryWithNullIdThenRejectDeletion() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteRepository(null));

        verifyNoInteractions(repository, registryProxy);
    }

    @Test
    void whenStoredRepositoryIdIsMalformedThenSelectFirstRepository() {
        when(repository.count()).thenReturn(1L);
        PluginRepository first = mock(PluginRepository.class);
        when(first.getId()).thenReturn(5L);
        when(registryProxy.readRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID))
                .thenReturn(Optional.of("not-a-number"));
        when(repository.findAllByOrderByNameAsc()).thenReturn(java.util.List.of(first));

        assertEquals(first, service.getSelectedRepository());

        verify(registryProxy).saveRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID, "5");
        verify(repository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void whenNormalizeRepositoryUrlWithUnsupportedSchemeThenRejectUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> PluginRepositoryService.normalizeUrl("file:///tmp/maven-repository"));
    }

    @Test
    void whenNormalizeRepositoryUrlWithCredentialsThenRejectUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> PluginRepositoryService.normalizeUrl("https://user:password@repo.example.com/maven"));
    }

    @Test
    void whenNoRepositoryExistsThenCreateDefaultRepository() {
        when(repository.count()).thenReturn(0L);
        PluginRepository saved = mock(PluginRepository.class);
        when(saved.getId()).thenReturn(1L);
        when(repository.save(org.mockito.ArgumentMatchers.any(PluginRepository.class))).thenReturn(saved);
        when(repository.findAllByOrderByNameAsc()).thenReturn(java.util.List.of(saved));

        service.findAll();

        org.mockito.ArgumentCaptor<PluginRepository> captor =
                org.mockito.ArgumentCaptor.forClass(PluginRepository.class);
        verify(repository).save(captor.capture());
        assertEquals("Default plugin repository", captor.getValue().getName());
        assertEquals(org.pacos.config.repository.data.AppRepository.DEFAULT_PLUGIN_REPOSITORY_URL,
                captor.getValue().getUrl());
        verify(registryProxy).saveRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID, "1");
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "https://repo.example.com/maven?token=value",
            "https://repo.example.com/maven#snapshot",
            "repo.example.com/maven"
    })
    void whenNormalizeRepositoryUrlWithQueryFragmentOrRelativePathThenRejectUrl(String url) {
        assertThrows(IllegalArgumentException.class, () -> PluginRepositoryService.normalizeUrl(url));
    }

    @Test
    void whenGetSelectedRepositoryWithoutStoredIdThenSelectFirstRepository() {
        when(repository.count()).thenReturn(1L);
        PluginRepository first = mock(PluginRepository.class);
        when(first.getId()).thenReturn(3L);
        when(repository.findAllByOrderByNameAsc()).thenReturn(java.util.List.of(first));

        assertEquals(first, service.getSelectedRepository());

        verify(registryProxy).saveRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID, "3");
    }

    @Test
    void whenGetSelectedRepositoryWithUnknownStoredIdThenSelectFirstRepository() {
        when(repository.count()).thenReturn(1L);
        PluginRepository first = mock(PluginRepository.class);
        when(first.getId()).thenReturn(4L);
        when(registryProxy.readRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID))
                .thenReturn(Optional.of("999"));
        when(repository.findById(999L)).thenReturn(Optional.empty());
        when(repository.findAllByOrderByNameAsc()).thenReturn(java.util.List.of(first));

        assertEquals(first, service.getSelectedRepository());

        verify(registryProxy).saveRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID, "4");
    }

    @Test
    void whenAddRepositoryWithDuplicateUrlThenRejectRepository() {
        when(repository.count()).thenReturn(1L);
        when(repository.existsByUrlIgnoreCase("https://repo.example.com/maven")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.addRepository("Partner", "https://repo.example.com/maven///"));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(registryProxy);
    }

    @Test
    void whenAddRepositoryWithBlankNameThenRejectRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addRepository("  ", "https://repo.example.com/maven"));

        verifyNoInteractions(repository, registryProxy);
    }

    @Test
    void whenDeleteLastRepositoryThenRejectDeletion() {
        PluginRepository onlyRepository = mock(PluginRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(onlyRepository));
        when(repository.count()).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.deleteRepository(1L));

        verify(repository, never()).delete(org.mockito.ArgumentMatchers.any(PluginRepository.class));
        verifyNoInteractions(registryProxy);
    }

    @Test
    void whenDeleteUnknownRepositoryThenRejectDeletion() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.deleteRepository(9L));

        verify(repository, never()).delete(org.mockito.ArgumentMatchers.any(PluginRepository.class));
        verifyNoInteractions(registryProxy);
    }

    @Test
    void whenConvertUnpersistedRepositoryThenRejectRepository() {
        PluginRepository repository = new PluginRepository("Temporary", "https://repo.example.com/maven");

        assertThrows(IllegalArgumentException.class, () -> service.toAppRepository(repository));
    }

    @Test
    void whenAddRepositoryWithDuplicateNameThenRejectRepository() {
        when(repository.count()).thenReturn(1L);
        when(repository.existsByNameIgnoreCase("Official")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.addRepository(" Official ", "https://repo.example.com/maven"));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(registryProxy);
    }

    @Test
    void whenAddRepositoryThenSelectNewRepository() {
        when(repository.count()).thenReturn(1L);
        PluginRepository saved = mock(PluginRepository.class);
        when(saved.getId()).thenReturn(15L);
        when(repository.save(org.mockito.ArgumentMatchers.any(PluginRepository.class))).thenReturn(saved);

        PluginRepository result = service.addRepository("Partner", "https://repo.example.com/maven///");

        assertEquals(saved, result);
        org.mockito.ArgumentCaptor<PluginRepository> captor =
                org.mockito.ArgumentCaptor.forClass(PluginRepository.class);
        verify(repository).save(captor.capture());
        assertEquals("Partner", captor.getValue().getName());
        assertEquals("https://repo.example.com/maven", captor.getValue().getUrl());
        verify(registryProxy).saveRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID, "15");
    }

    @Test
    void whenDeleteSelectedRepositoryThenSelectReplacement() {
        PluginRepository selected = mock(PluginRepository.class);
        PluginRepository replacement = mock(PluginRepository.class);
        when(selected.getId()).thenReturn(1L);
        when(replacement.getId()).thenReturn(2L);
        when(repository.findById(1L)).thenReturn(Optional.of(selected));
        when(repository.findById(2L)).thenReturn(Optional.of(replacement));
        when(repository.count()).thenReturn(2L);
        when(registryProxy.readRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID)).thenReturn(Optional.of("1"));
        when(repository.findAllByOrderByNameAsc()).thenReturn(java.util.List.of(selected, replacement));

        service.deleteRepository(1L);

        verify(registryProxy).saveRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID, "2");
        verify(repository).delete(selected);
    }

    @Test
    void whenGetSelectedAppRepositoryThenReturnConfiguredRepositoryCoordinate() {
        when(repository.count()).thenReturn(1L);
        PluginRepository selected = mock(PluginRepository.class);
        when(selected.getId()).thenReturn(7L);
        when(selected.getUrl()).thenReturn("https://repo.example.com/maven");
        when(registryProxy.readRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID)).thenReturn(Optional.of("7"));
        when(repository.findById(7L)).thenReturn(Optional.of(selected));

        org.pacos.config.repository.data.AppRepository result = service.getSelectedAppRepository();

        assertEquals("pacos-plugin-repo-7", result.id());
        assertEquals("https://repo.example.com/maven", result.url());
    }

    @Test
    void whenSelectRepositoryThenPersistSelectedRepositoryId() {
        PluginRepository selected = mock(PluginRepository.class);
        when(selected.getId()).thenReturn(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(selected));

        service.selectRepository(7L);

        verify(registryProxy).saveRegistry(RegistryName.ACTIVE_PLUGIN_REPOSITORY_ID, "7");
    }

    @Test
    void whenSelectUnknownRepositoryThenRejectSelection() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.selectRepository(9L));

        verifyNoInteractions(registryProxy);
    }
}
